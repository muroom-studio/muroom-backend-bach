# [RAW] AWS 인프라 & 배포 조사 결과 (에이전트 원본, 2026-08-02)

> 소스: `infra/*.tf` 전체, user-data 템플릿, application-{prod,dev}.yml, git 히스토리의 deploy 커밋.

## 한눈에 보기

- 리전 `ap-northeast-2`(서울). **전부 ARM/Graviton**(`t4g.*`, arm64 AMI).
- **prod/dev 두 환경이 VPC 하나(10.0.0.0/16) 안에 공존** — SG/클러스터/서브넷/타깃그룹으로만 분리.
- 인터넷 → Route53/DNS(테라폼 외부) → **ALB**(퍼블릭 2a+2b, 80→301→443, ACM `api.muroom.kr`+`dev-api.muroom.kr`, 호스트 헤더 라우팅, **default action = 404 고정 응답**) → **ECS on EC2**(prod `t4g.medium` ASG min1/max5, dev `t4g.small` min1/max1, awsvpc/ENI, target_type=ip) → Spring Boot :8080
- **PostgreSQL 17+PostGIS: EC2 자체 운영**(RDS 아님) — prod `t4g.small`/dev `t4g.micro`, 인스턴스에서 직접 Docker 이미지 빌드(`postgres:17-bookworm`+postgis), 전용 암호화 gp3 EBS, **프라이빗 2a 단일 AZ**
- **Valkey 8.1.5: EC2 자체 운영**(ElastiCache 아님) — prod `t4g.micro`/dev `t4g.nano`, ACL 유저, **프라이빗 2a**
- 아웃바운드: **NAT 인스턴스**(`t4g.nano`, nftables masquerade, 퍼블릭 2a, EIP) 단일. VPC 엔드포인트는 **S3 Gateway(무료)만** — ECR/SecretsManager/CloudWatch는 NAT 경유.
- S3: 앱 4버킷(prod/dev × public/private) + 백업 4버킷(postgres/valkey).
- Secrets Manager: postgres 자격증명(SAR Lambda로 **자동 로테이션 — prod 7일/dev 1일**), valkey, JWT 키(`# TODO: Deprecate`), **외부 API 키 12개를 env당 1개 시크릿에 번들**(주석에 "비용 절감 차원" 명시).
- 백업 3중: DLM EBS 스냅샷(prod 7일/dev 2일) + `pg_dump`→S3(systemd 타이머, 02:30 KST, INTELLIGENT_TIERING) + **WAL 연속 아카이브→S3**(10초 주기 systemd 루프) + Valkey BGSAVE RDB→S3(03:00 KST).
- 모니터링: SNS→이메일(root@muroom.kr), 알람 4개(NAT status/CPU70%, prod Postgres status/CPU80%)뿐. ALB 5xx/ECS/디스크/백업 실패 알람 없음.
- 상태 관리: S3 `muroom-terraform-state-backend` + DynamoDB 락(별도 부트스트랩 모듈).
- **배포: CI/CD 없음.** 로컬 docker build → ECR push(dev-`<sha>` / prod-v`YY.MM.DD.<letter>` 캘린더 버전) → `infra/ecs_task.tf`의 image 라인 수동 수정 → `deploy: <tag>` 커밋(1파일 1라인 변경으로 실증: `a65ae35`, `f4c22c3`, `c223285` 등) → `terraform apply` → ECS 롤링(prod 100%/200% + circuit breaker 자동 롤백, dev 0%/100% 다운타임 허용).
- 운영 접근: **SSM Session Manager 전용** — SSH 키페어/배스천 없음. `ssm-user`를 docker 그룹에 추가.

## 상세

### 네트워크
- 퍼블릭 10.0.11.0/24(2a), 10.0.12.0/24(2b); 프라이빗 10.0.21.0/24(2a), 10.0.22.0/24(2b). 퍼블릭 RT 1개, 프라이빗 RT 1개 공유.
- NAT 인스턴스: AL2023 minimal arm64, `source_dest_check=false`, ip_forward + nftables masquerade, `prevent_destroy`, `ignore_changes=[ami, user_data]`. NAT GW(~$32/mo+데이터) vs t4g.nano(~$3/mo).
- SG 7개, 규칙은 전부 개별 `aws_vpc_security_group_*_rule` 리소스. 최소권한 체인: ALB→ECS(8080)→Postgres(5432)/Valkey(6379); 로테이션 Lambda SG→양쪽 Postgres.

### 컴퓨트
- prod 태스크: cpu 1920/mem 3072 (t4g.medium 2vCPU/4GB에 여유 주석). dev: 1024/1536.
- 로그: `/ecs/muroom-prod` 14일 / dev 3일 (파일 헤더 주석 "1일"은 스테일).
- env 주입: SPRING_PROFILES_ACTIVE, AWS_REGION, S3 버킷 2개. secrets 주입: DB 5개, VALKEY 4개, JWT, 외부키 12개 (`arn:key::` JSON-key 문법).
- prod 서비스: `distinctInstance` 제약 + 100%/200% → 배포 시 ASG가 2대로 늘어나야 함 (커밋 `2dac014 fix: ecs prod 이중화 이슈 해결`이 이 지점의 사고 흔적 [추론]).
- 헬스체크: `GET /actuator/health/readiness`, 30s/10s/3/3, grace 180s — `WarmupListener`의 명시적 readiness 발행과 맞물림.

### 데이터
- Postgres user_data: EBS를 volume-id 시리얼로 탐지(60초 재시도 루프), xfs 포맷/fstab, Secrets Manager에서 자격증명, **인스턴스 위에서 PostGIS 이미지 빌드**, `shared_buffers=512MB`, `max_connections=60`, `wal_level=replica`, `archive_mode=on`, 마지막에 `CREATE EXTENSION IF NOT EXISTS postgis`.
- Hikari max 10 × desired 1 = 60 커넥션 한도 내 여유.
- Valkey user_data: `--user default off`, ACL 유저 생성, `--appendonly yes`, AOF rewrite 100%/64mb, `--save 60 1`. THP 비활성 systemd 유닛, somaxconn 튜닝.
- **로테이션 Lambda는 SAR `SecretsManagerRDSPostgreSQLRotationSingleUser`** (RDS용)를 자체 운영 Postgres에 적용 — libpq로 동작은 가능하나 검증 여부는 질문.
- Valkey 시크릿엔 `ignore_changes` 없음 → apply가 random_password 재생성하면 실행 중 컨테이너(부팅 시에만 ACL 설정)와 어긋날 수 있음.

### 앱 ↔ 인프라 연결 (application-prod/dev.yml)
- **aws-advanced-jdbc-wrapper**: `jdbc:aws-wrapper:postgresql://…`, `wrapperPlugins: awsSecretsManager` — 자격증명이 ①ECS secrets 주입 ②런타임 wrapper 재조회의 **이중 경로**. 인증 실패 시 재조회가 로테이션을 재배포 없이 견디게 함. 이래서 태스크 롤(실행 롤 아님)에도 GetSecretValue 필요.
- Flyway: enabled + `baseline-on-migrate: true`, 태스크 기동마다 실행. `ddl-auto: validate`.
- `server.forward-headers-strategy: native` (ALB 뒤). presign TTL prod 5분/dev 10분. Swagger prod 비활성.

### 보안 관찰
- `infra/terraform.tfvars`가 워킹 트리에 존재하며 **평문 프로덕션 시크릿 포함** — 단 `.gitignore`(`*.tfvars`)로 git 미추적 확인. 로컬 파일 노출이지 리포 유출 아님.
- env 단위 시크릿 정책이 4개 시크릿 전부에 대한 접근을 Postgres/Valkey/ECS 실행/태스크 롤 모두에 부여 — Valkey 박스가 Postgres/JWT 시크릿 읽기 가능.
- 퍼블릭 버킷: `s3:GetObject` Principal:* 정책, block_public_policy=false 의도적.
- WAF/ALB 액세스 로그/GuardDuty 없음.

### 단일 장애점 (명시적 관찰)
- **데이터 계층 전체 + NAT + dev ECS가 AZ 2a에 단일 배치.** prod ECS만 2a+2b인데 데이터가 2a뿐이라 **prod의 멀티 AZ는 사실상 장식** — 2a 장애 = 전면 장애. EBS가 AZ 고정이라 구조적으로 잠김.
- NAT 인스턴스 단일 — 다운 시 양 환경의 모든 아웃바운드(ECR pull, Secrets, S3, 외부 API) 단절. NAT 알람 2개가 존재하는 이유.

## 미해결 질문 (에이전트 제기)

1. DNS/ACM 발급·검증이 테라폼 밖 — Route53 수동 관리?
2. docker build/push 자동화 없음 — 순수 수동? prod 캘린더 태그 수동 채번?
3. prod 배포 시 ASG 2대 확장이 안정적인가 (`2dac014` 커밋 정황)?
4. RDS용 로테이션 Lambda를 자체 운영 PG에 적용 — 엔드투엔드 검증됐나? 컨테이너 첫 부팅 때 설정된 `POSTGRES_PASSWORD`와 동기화는?
5. WAL 리스토어/PITR 런북 부재 — 문서화 어디에?
6. Postgres/Valkey 인스턴스 유실 시 재구축 경로 (`ignore_changes` + `prevent_destroy` 조합에서)?
7. dev Valkey 데이터 볼륨 1GB (prod 10GB) — 의도?
8. DLM 정책 태그 셀렉터가 Valkey 볼륨도 포섭 — 의도?
9. ecs_task.tf 헤더 주석 "보관 1일" vs 실제 14/3일 — 어느 쪽이 진실?
10. JWT 시크릿 `# TODO: Deprecate and Delete` — 대체 계획?
