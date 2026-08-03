# 03. 딥다이브: AWS 인프라 — Terraform, 비용 최적화, 그리고 운영

> 작성일: 2026-08-02 · 근거: `infra/*.tf` 전체 + user-data 템플릿 + application-{prod,dev}.yml + git 히스토리(딥다이브 diff 판독)
> 표기: **[확인]** = 커밋/파일 근거 병기, **[추론]** = 정황 추론. 비용 수치는 전부 **대략적 추정치**.
> 저자: 이 영역의 커밋(infra 16건 + deploy 7건 전부)은 **monte-kim 단독 저자 [확인]**. 예외적으로 타 저자 커밋 인용 시 명시.

## 1. 요약

- **prod + dev 2개 환경 전체를 소규모(추정 월 수만 원대) 예산으로 운영**하는 스타트업 백엔드 인프라. 관리형 서비스(RDS·ElastiCache·NAT GW)를 의도적으로 배제하고 전부 Graviton(ARM) EC2 자체 운영으로 대체.
- 2025-11 수동 인프라로 시작 → **2026-02-03 Terraform 1차 적용(43be918)** → 02-04 하루 만에 fix 9건의 실전 트러블슈팅 → 캘린더 버전 수동 배포 체계(`prod-vYY.MM.DD.<letter>`) 정착.
- 백업은 3중(EBS 스냅샷 + pg_dump→S3 + **WAL 10초 주기 S3 쉬핑**), 시크릿은 Secrets Manager 자동 로테이션(prod 7일)까지 갖춘 반면, 데이터 계층은 단일 AZ·모니터링은 알람 4개뿐인 **의도된 비대칭 투자**가 특징.

## 2. 현재 설계

### 한눈에 보기 [확인 — infra/*.tf 전수]

```
인터넷 → Route53(테라폼 외부) → ALB(퍼블릭 2a+2b, 80→301→443, ACM, 호스트헤더 라우팅, default=404)
       → ECS on EC2(awsvpc/ENI, target_type=ip) → Spring Boot :8080
       → PostgreSQL 17+PostGIS (EC2 자체 운영, 프라이빗 2a)
       → Valkey 8.1.5 (EC2 자체 운영, 프라이빗 2a)
아웃바운드: NAT "인스턴스"(t4g.nano, nftables masquerade) 단일 + S3 Gateway 엔드포인트(무료)
```

- 리전 `ap-northeast-2`(서울), **전 인스턴스 ARM/Graviton**(`t4g.*`, arm64 AMI).
- **prod/dev가 VPC 하나(10.0.0.0/16)에 공존** — SG 7개/클러스터/타깃그룹으로만 분리.

### 계층별 압축표

| 계층 | prod | dev | 비고 [확인] |
|---|---|---|---|
| 네트워크 | 퍼블릭 10.0.11/12.0/24(2a·2b), 프라이빗 10.0.21/22.0/24 | 공유 | RT는 퍼블릭/프라이빗 각 1개 공유 |
| 컴퓨트 | ECS on EC2 `t4g.medium` ASG min1/max5, task cpu1920/mem3072 | `t4g.small` min1/max1, 1024/1536 | 롤링 prod 100%/200%+circuit breaker, dev 0%/100% |
| DB | PostgreSQL 17+PostGIS `t4g.small`, 암호화 gp3 EBS | `t4g.micro` | 인스턴스 위에서 PostGIS 이미지 직접 빌드 |
| 캐시 | Valkey 8.1.5 `t4g.micro`, ACL 유저 | `t4g.nano` | AOF + `save 60 1` |
| 시크릿 | Secrets Manager — DB 자격증명 **자동 로테이션 7일** | 1일 | 외부 API 키 12개는 env당 시크릿 1개에 번들("비용 절감" 주석) |
| 백업 | DLM EBS 스냅샷 7일 + pg_dump 02:30 KST→S3 + **WAL 10초 루프→S3** + Valkey RDB 03:00→S3 | 스냅샷 2일 | 백업 전용 S3 4버킷, INTELLIGENT_TIERING |
| 모니터링 | SNS→이메일, 알람 4개(NAT status/CPU70, prod PG status/CPU80) | 없음 | ALB 5xx/ECS/디스크/백업 실패 알람 부재 |
| 상태 관리 | S3 `muroom-terraform-state-backend` + DynamoDB 락 | 공유 | 별도 부트스트랩 모듈 |
| 접근 | **SSM Session Manager 전용** — SSH 키/배스천 없음 | 동일 | `ssm-user`를 docker 그룹에 추가 |

## 3. 비용 최적화 결정 인벤토리

> 절감액은 서울 리전 정가 기준 **대략 추정**. [확인]은 코드/주석 근거 존재.

| 선택 | 절감(추정) | 포기한 것 |
|---|---|---|
| NAT GW 대신 **NAT 인스턴스**(t4g.nano+nftables) [확인 nat_instance.tf] | ~$32/mo + 데이터 처리비 → ~$3/mo | 관리형 가용성 — NAT 죽으면 양 환경 아웃바운드 전멸 |
| RDS 대신 **EC2 자체 운영 PostgreSQL** [확인 postgres_instance.tf] | RDS t4g.small 대비 월 수만 원 | 자동 페일오버, 관리형 PITR, 마이너 패치 자동화 |
| ElastiCache 대신 **EC2 자체 운영 Valkey** [확인 valkey_instance.tf] | ElastiCache 최소 사양 대비 수만 원 | 관리형 복제/백업/모니터링 |
| **전부 Graviton(t4g)** [확인 — 전 인스턴스 arm64] | 동급 x86 대비 ~20% | x86 전용 바이너리 호환성(로컬 docker build도 arm 통일 필요) |
| VPC 엔드포인트는 **S3 Gateway(무료)만** [확인 vpc.tf] | 인터페이스 엔드포인트 개당 ~$8/mo × 3~4개 | ECR/Secrets/CloudWatch가 NAT 단일 경로 의존 |
| 외부 API 키 12개를 **env당 시크릿 1개에 번들** [확인 — secrets_manager.tf 주석 "비용 절감 차원"] | 시크릿 개당 $0.4/mo × ~22개 | 키별 접근 격리·개별 로테이션 |
| prod/dev **VPC 단일 공존** [확인 vpc.tf] | VPC 자체는 무료지만 NAT/엔드포인트 이중화 회피 | 계정/네트워크 레벨 환경 격리 |
| dev ECS `0%/100%` 배포 [확인 ecs_service.tf] | 배포 중 2대 유지 비용 | dev 배포 중 다운타임(의도적 허용) |
| CloudWatch 로그 보관 **prod 14일/dev 3일** [확인 ecs_task.tf] | 장기 보관 비용 | 장기 로그 포렌식 |
| pg_dump S3 **INTELLIGENT_TIERING** [확인 user-data] | 스토리지 자동 계층화 | 소용량에선 미미 — 사실상 습관적 최적화 |
| **CI/CD 미구축** — 로컬 빌드+수동 apply [확인 deploy 커밋] | CodePipeline/GitHub Actions 러너 비용 0 | 배포 자동화·감사 추적(대신 deploy 커밋으로 수동 기록) |
| 모니터링 최소화(알람 4개) [확인 monitoring.tf] | CloudWatch 알람/대시보드 비용 | 장애 조기 감지 — §6 리스크로 연결 |

## 4. 진화 과정

### 4-1. 수동 인프라 시대 (2025-11 ~ 2026-01) [확인 — git 고고학]

- **2025-11-09 `1c0075f`** (베타 기능 첫 커밋): **Dockerfile + application-dev/prod.yml이 앱 코드와 함께 등장** — 11월부터 AWS에 수동 배포가 있었다는 물증. 이때의 prod.yml은:
  - `jdbc:postgresql://${DB_HOST}:...` 등 **전부 환경변수 주입**(하드코딩은 아니었으나 시크릿 관리 체계는 없음 — 주입 주체는 코드 밖 [추론: 수동 env 관리]).
  - Hikari `maximum-pool-size: 20`, Redis 설정은 **주석 처리** 상태(아직 캐시 미사용).
  - 로깅 패키지가 `com.moty.solarserver` — **이전 개인 프로젝트에서 복사해온 흔적 [확인]**. (현재는 `kr.muroom.muroombackendbach`로 정리됨.)
- **2025-12-08 `8b9607c`** (저자 **2-say**): "aws redis 설정" — REDIS_HOST/PORT/PASSWORD 활성화. 12월부터 AWS 어딘가에 Redis가 존재 [확인]. 토폴로지는 코드로 알 수 없음(→ 질문 목록).
- 이후 2026-01까지 prod.yml 변경은 OAuth redirect·JWT 만료 등 앱 설정 위주 — **인프라 구조 변경 없이 3개월 운영**.

### 4-2. Terraform 1차 적용 — 2026-02-03 `43be918` [확인]

- 단일 커밋에 **infra/*.tf 27개 파일, +3,840라인**: VPC/ALB/ASG/ECS(cluster·service·task)/ECR/IAM(464라인)/NAT 인스턴스/Postgres·Valkey user-data 템플릿(각 209/211라인)/S3 8버킷/Secrets Manager/모니터링/state 백엔드까지 한 번에.
- 같은 커밋에서 앱 설정도 이관: `REDIS_*` → `VALKEY_*`(username 추가 = ACL 도입), `DB_DATABASE` → `DB_NAME`. redis/ 디렉터리 → valkey/ 리네임.
- **[추론]** 커밋 전 로컬에서 상당 기간 plan/apply를 반복한 뒤 일괄 커밋한 형태(중간 커밋 없음).

### 4-3. 02-04 트러블슈팅 연대기 — 하루 9 커밋 [확인, 시간순]

| 시각 | 커밋 | 문제 → 해결 (diff 판독) |
|---|---|---|
| 08:45 | `56ab511` ssm 연결 안 됨 1차 | **SG egress를 `referenced_security_group_id = NAT SG`로 정의** → NAT "인스턴스" 경유 트래픽엔 SG 참조가 성립하지 않아 SSM 에이전트가 엔드포인트에 못 나감. **egress를 `cidr_ipv4 = 0.0.0.0/0`으로 교체**(ECS/PG/Valkey 3계층 전부) + ECS 런치템플릿에 ssm-user docker 그룹 추가 |
| 16:02 | `bcf59c0` postgis 적용 안 됨 | 최초엔 **`kartoza/postgis:17-3.5` 이미지** 사용(`POSTGRES_PASS`, `ALLOW_IP_RANGE` 환경변수) — 적용 실패. **공식 `postgres:17-bookworm`에 postgis 패키지를 얹어 인스턴스 위에서 직접 빌드**하는 방식으로 전환. 데이터 경로도 `/var/lib/postgresql` → `/var/lib/postgresql/data`로 정정, `chown 999:999` 복원. **"인스턴스에서 이미지 직접 빌드"라는 현재 설계는 이 실패의 산물** |
| 16:30 | `a06b278` docker 보안 | `docker exec -e PGPASSWORD='평문'` 으로 pg_dump 실행 — **프로세스 목록/user-data 로그에 암호 노출**. `.pgpass`(600) 파일 생성 후 컨테이너에 주입하는 방식으로 교체 |
| 16:35 | `227ccf0` delete protection | 검증 끝난 리소스에 일괄 안전핀: ALB `enable_deletion_protection`, ECR/Lambda/NAT/PG/Valkey/S3/state 백엔드 `prevent_destroy` **false → true** (9개 파일 32라인) |
| 낮 | `0cea336` s3 경로 정정 | 앱 쪽 1라인 — 업로드 요청 DTO의 S3 경로 오타 |
| 18:37 | `71fd7fa` 로테이션 설정(feat) | **aws-advanced-jdbc-wrapper 2.6.8 도입**: `wrapperPlugins: awsSecretsManager` + Hikari 20→10, `max-lifetime` 30분, `connection-timeout` 3초로 재튜닝. Lettuce 커넥션 풀도 이때 추가 |
| 18:51 | `b45f1d1` 로테이션 fix 1차 | 드라이버는 `software.amazon.jdbc.Driver`로 바꿨는데 **URL 스킴이 `jdbc:postgresql`인 채** — `jdbc:aws-wrapper:postgresql`로 정정 |
| 19:03 | `3a4527e` 로테이션 fix 2차 | 의존성 오답: `io.awspring.cloud:spring-cloud-aws-starter-secrets-manager` 추가(잘못된 라이브러리) |
| 19:06 | `0c9d536` "3차!!!!!!!!!!!!!!!" | **정답: `software.amazon.awssdk:secretsmanager`** — wrapper의 awsSecretsManager 플러그인이 요구하는 건 AWS SDK v2 모듈이었음. 1라인 교체로 종결 |

- 로테이션 3연전 사이사이 dev 배포 4회(`5b5af79`→`b4d8d6c`→`a6049c1`→`3853b04`)로 실배포 검증 후, 당일 **첫 캘린더 버전 prod 배포 `c223285` (prod-v26.02.04.A)** [확인].

### 4-4. ECS prod 이중화 이슈 — 2026-02-17 `2dac014` [확인]

diff 판독 결과, 원인은 두 갈래:
1. **Terraform과 Capacity Provider의 desired_capacity 줄다리기** — ASG `desired_capacity`를 TF가 관리해 apply마다 1로 되돌림 → 배포 시 200%로 늘어난 2번째 인스턴스가 회수됨. `lifecycle { ignore_changes = [desired_capacity] }` 추가로 CP에 위임.
2. **task cpu 2048 = 인스턴스 등록 용량 전부** — t4g.medium(2048 유닛)에 여유가 없어 배치 실패. **cpu 1920으로 감축**("시스템 여유분" 주석). `target_capacity`도 80→100으로(상시 여분 인스턴스를 띄우지 않도록 — 비용 우선).
- 같은 날 `703d0f5`(세션 미인증 500) 수정과 함께 `prod-v26.02.17.A` 배포 [확인 f4c22c3].

### 4-5. Redis connection fail — 2026-02-22 `3594f6b` [확인]

- prod/dev yml에서 `ssl.enabled: true` **제거** 2라인이 전부. 자체 운영 Valkey는 TLS 미종단인데 Lettuce가 TLS 핸드셰이크를 시도 — 관리형(ElastiCache) 전제의 설정이 남아 있던 것 [추론: ElastiCache 시절 혹은 예제 복사 잔재]. dev 배포 `a65ae35`로 검증.

### 4-6. 배포 체계 정착 [확인]

- **CI/CD 없음.** 로컬 docker build → ECR push → `infra/ecs_task.tf`의 image 태그 1라인 수동 수정 → `deploy: <tag>` 커밋 → `terraform apply` → ECS 롤링.
- 태그 체계: dev는 `dev-<커밋해시>`(추적성), prod는 **캘린더 버전 `prod-vYY.MM.DD.<letter>`**(하루 다중 배포는 A/B/C).
- deploy 커밋 7건 전부 1파일 1라인 변경 [확인 a65ae35, f4c22c3, c223285 등] — **git 로그 자체가 배포 대장 역할**을 하는 셈.

## 5. 흥미로운 엔지니어링 3선

### ① RDS용 SAR 로테이션 Lambda를 자체 운영 PG에 + wrapper 런타임 재조회

- Secrets Manager 로테이션에 SAR의 **`SecretsManagerRDSPostgreSQLRotationSingleUser`**(RDS 전용으로 배포되는 Lambda)를 자체 운영 Postgres에 그대로 물림 [확인 infra/lambda.tf] — libpq 프로토콜은 동일하므로 동작. 전용 로테이션 Lambda를 직접 짜는 대신 검증된 SAR 스택 재사용.
- 로테이션 후 앱이 죽지 않는 비결이 **자격증명 이중 경로**: ① ECS secrets로 기동 시 주입 + ② `aws-advanced-jdbc-wrapper`의 `awsSecretsManager` 플러그인이 **인증 실패 시 Secrets Manager를 런타임 재조회** [확인 application-prod.yml]. Hikari `max-lifetime: 1800000`(30분)으로 커넥션이 로테이션 주기보다 훨씬 짧게 순환되는 것도 세트. 이 때문에 태스크 **실행 롤이 아닌 태스크 롤**에도 GetSecretValue 권한 부여 [확인 iam.tf].
- 단, 컨테이너 첫 부팅 `POSTGRES_PASSWORD`와의 초기 동기화 검증 여부는 미확인(→ 질문 목록).

### ② WAL 10초 S3 쉬핑 + 3중 백업

- user-data가 systemd 서비스로 **10초 주기 무한 루프**를 심어 `archive_status`의 미전송 WAL 세그먼트를 S3로 밀어냄 [확인 postgres_user_data.sh.tpl] — `wal_level=replica` + `archive_mode=on`과 결합해 **RPO 초 단위의 수제 PITR 소재** 확보.
- 여기에 DLM EBS 스냅샷(7일) + 02:30 pg_dump 논리 백업까지 **성격이 다른 3중 백업**(블록/논리/연속 아카이브). RDS 없이 RDS급 백업 체계를 EBS+S3 비용만으로 재현.
- 리스토어 런북은 부재 [확인 — 리포 내 문서 없음] — 백업은 3중인데 복구는 미리허설(→ §6).

### ③ 콜드스타트 대책 세트 — 단일 커밋의 3단 콤보

- **`bcdff79`(2026-01-25, Terraform 이관 "전") 한 커밋에 3가지가 동시 도입** [확인 diff]:
  1. `spring.mvc.servlet.load-on-startup: 1` — DispatcherServlet 선초기화.
  2. `annotationProcessor 'org.springframework:spring-context-indexer'` — 컴포넌트 스캔 인덱싱.
  3. **`WarmupListener`**(ApplicationRunner): DB 워밍업 쿼리(`musicianRepository.count()`) 후 **`ReadinessState.ACCEPTING_TRAFFIC`를 명시 발행**.
- 이 커밋에서 actuator 의존성 자체가 처음 추가됨 — readiness 프로브가 콜드스타트 해결 목적으로 도입된 것 [확인].
- 2월 Terraform의 ALB 헬스체크가 `GET /actuator/health/readiness`(grace 180s)를 보게 되면서 [확인 ecs_service.tf], "워밍업 끝나기 전엔 트래픽 안 받는다"가 앱-인프라 양쪽에서 맞물림. **1월의 앱 수정이 2월 인프라 설계의 복선**이었던 구조.

## 6. 제약 / 리스크

| # | 현상 → 영향 → 개선안 한 줄 |
|---|---|
| 1 | **데이터 계층 전체(PG·Valkey·EBS)가 AZ 2a 단일** [확인] → prod ECS가 2a+2b여도 2a 장애 = 전면 장애(멀티 AZ는 사실상 장식), EBS가 AZ 고정이라 구조적 잠김 → 스트리밍 리플리카 1대를 2b에 두거나 RDS Multi-AZ 전환 검토 |
| 2 | **NAT 인스턴스 단일(t4g.nano)** [확인] → 다운 시 양 환경의 ECR pull/Secrets/외부 API 전부 단절(알람 2개가 이 공포의 증거) → 자동 복구 스크립트(EIP 재부착) 또는 최소 ECR·Secrets 인터페이스 엔드포인트 추가 |
| 3 | **세션이 in-memory(JSESSIONID) + ASG max5** [확인 — Spring Session 미도입] → 스케일아웃/롤링 시 세션 유실·로그인 튕김 → Valkey가 이미 있으므로 Spring Session Data Redis 한 방 |
| 4 | **모니터링 공백** — 알람 4개뿐, ALB 5xx/ECS 태스크/디스크/백업 실패 무감시 [확인 monitoring.tf] → 백업이 조용히 실패해도 모름 → 백업 systemd 타이머 실패 시 SNS 발행 + ALB 5xx 알람부터 |
| 5 | **`infra/terraform.tfvars`에 평문 프로덕션 시크릿** [확인 — 단 `.gitignore`로 git 미추적 확인, 리포 유출 아님] → 로컬 머신 분실 = 전체 시크릿 노출 → SOPS/SSM Parameter 참조로 tfvars에서 시크릿 제거 |
| 6 | **WAL/PITR 복구 런북 부재** [확인] → 실제 장애 시 복구 시간 예측 불가 → 분기 1회 리스토어 리허설 문서화 |

## 7. 면접 예상 질문 씨앗

1. **"왜 RDS를 안 썼나?"** — 답변 포인트: 월 비용 절감(추정 수만 원)과 학습 목적의 트레이드오프를 인지한 선택. 포기한 것(Multi-AZ, 관리형 PITR)을 3중 백업 + WAL 쉬핑으로 부분 보상했고, SAR 로테이션 Lambda 재사용으로 RDS급 시크릿 로테이션까지 재현. "규모가 커지면 RDS로 가는 기준선(트래픽/팀 크기)"을 스스로 제시하면 강함.
2. **"NAT 인스턴스가 죽으면 어떻게 되나?"** — 양 환경 아웃바운드 전멸을 인지하고 status/CPU 알람 2개를 걸어둠 [확인]. S3만 Gateway 엔드포인트로 우회시켜 백업 경로는 생존. 개선 시나리오(EIP 자동 재부착, 핵심 엔드포인트 추가, NAT GW 전환 임계점)를 비용 수치와 함께 말할 것.
3. **"스케일아웃 하려면 뭐부터 해야 하나?"** — 순서 감각: ① 세션 외부화(Spring Session + 기존 Valkey) ② PG `max_connections=60` vs Hikari 10×태스크수 재산정 ③ 데이터 계층 멀티 AZ. "ASG max5는 이미 있으니 병목은 컴퓨트가 아니라 상태"라는 프레임.
4. **"Secrets 로테이션 도입 때 뭐가 제일 어려웠나?"** — 02-04 3연전 실화 [확인 b45f1d1→0c9d536]: URL 스킴 누락 → 잘못된 스타터 의존성 → AWS SDK 모듈이 정답. 교훈: wrapper 플러그인의 클래스패스 요구사항을 문서가 아닌 dev 실배포 4회로 검증했고, 이중 경로(주입+런타임 재조회) 덕에 로테이션이 재배포 없이 무중단.
5. **"ECS 배포가 안 나가던 이중화 이슈의 원인은?"** — `2dac014` diff 그대로: TF가 desired_capacity를 되돌리는 상태 드리프트 + task cpu가 인스턴스 용량 100%라 배치 불가. 해결이 "인스턴스 추가"가 아니라 **ignore_changes로 소유권을 Capacity Provider에 넘기고 cpu 1920으로 여유 확보**였다는 점 — IaC와 오케스트레이터의 제어권 충돌을 이해한 사례.

---

### 부록: 이 문서의 커밋 색인

| 커밋 | 날짜 | 내용 |
|---|---|---|
| `1c0075f` | 2025-11-09 | 첫 배포 흔적(Dockerfile + prod.yml, solarserver 로깅 잔재) |
| `8b9607c` | 2025-12-08 | AWS Redis 설정 (저자 2-say) |
| `bcdff79` | 2026-01-25 | 콜드스타트 3단 콤보(WarmupListener + load-on-startup + context-indexer) |
| `43be918` | 2026-02-03 | Terraform 1차 적용 (+3,840라인) |
| `56ab511` → `bcf59c0` → `a06b278` → `0cea336` → `227ccf0` | 2026-02-04 | SSM/PostGIS/도커보안/S3경로/삭제보호 |
| `71fd7fa` → `b45f1d1` → `3a4527e` → `0c9d536` | 2026-02-04 | 로테이션 도입 + fix 3연전 |
| `c223285` | 2026-02-04 | 첫 prod 캘린더 배포 (prod-v26.02.04.A) |
| `2dac014`, `f4c22c3` | 2026-02-17 | ECS 이중화 해결 + prod-v26.02.17.A |
| `3594f6b`, `a65ae35` | 2026-02-22 | Valkey TLS 설정 제거 + dev 배포 |
