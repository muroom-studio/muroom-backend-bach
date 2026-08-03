# 08. 면접 대비 — STAR 스토리 + 예상 꼬리질문

> 용도: 영국 백엔드 포지션 면접. 근거는 전부 01~06 문서와 대응 — 면접 전 해당 딥다이브(03-*)를 다시 읽을 것.
> 대원칙: **구현 주어를 쓸 수 있는 건 본인 영역만** (studio·검색·인프라·filestorage·데이터 계층·JWT→세션 "결정"). Sehee 구현 영역은 "my teammate implemented / we decided"로.

---

## S1. 비용 제약 하의 인프라 설계 (플래그십 — "Tell me about a challenging project")

- **S**: Two-person student startup; grant fully allocated to marketing, so server costs came from our own pockets until a $1,000 credit arrived.
- **T**: Run production + dev for a geo-search service (Spring Boot, PostGIS, Redis-compatible cache) at minimum cost without losing operational safety.
- **A**: Migrated console-managed infra to Terraform; replaced NAT Gateway with a t4g.nano NAT instance; moved RDS to self-managed Postgres/PostGIS on EC2; Valkey on EC2 with ACL; single VPC/ALB with host-header routing; bundled 12 API keys into one secret per env. Compensated: 3-layer backup (DLM snapshots + nightly pg_dump + 10-second WAL shipping to S3), SAR rotation Lambda + AWS Advanced JDBC Wrapper for redeploy-free credential rotation, SSM-only access (no SSH keys).
- **R**: ~$150+ → ~$120/month for two environments. Accepted trade-offs consciously: single-AZ data tier, short backup retention.
- **꼬리질문 대비**:
  - *"Why not RDS?"* → 숫자로 시작: RDS 2환경 고정비 vs EC2 자체 운영, 사비 부담 시기. "포기한 것" 목록을 먼저 말하고 보상 장치를 설명 (관리형의 가치를 아는 사람으로 보임).
  - *"NAT instance dies — what happens?"* → 전 VPC 아웃바운드 단절. 알람 2개로 감지, 수동 복구. 규모상 수용한 리스크; 성장 시 NAT GW 회귀가 1순위.
  - *"How does rotation work without RDS?"* → SAR Lambda는 libpq로 동작하므로 자체 PG에도 적용 가능; 앱은 JDBC wrapper의 awsSecretsManager 플러그인이 인증 실패 시 재조회 → 무중단. dev를 1일 주기 카나리로 썼음.
  - *"Biggest mistake?"* → 고아 RDS 6개월($21/mo) — 크레딧이 청구서를 마취시킴. "지금은 태그 기반 비용 알람/주기 감사를 둘 것."
  - *"What would you do differently?"* → CI/CD: terraform plan-only 파이프라인 + 수동 apply 승인. (당시엔 오적용 공포로 보류 — 신중함의 근거로 설명 가능.)

## S2. JWT → 세션 전환 (결정·설계 본인 / 구현 팀원 — "Tell me about a technical decision you drove")

- **S**: We shipped OAuth login with JWT access/refresh; refresh tokens needed server-side state — issue, rotate, revoke — in Redis.
- **T**: Reassess: the token store was reimplementing session semantics with more moving parts.
- **A**: I argued and designed the switch: "If we're managing token state in Redis, we've built sessions with extra steps — let's use real sessions." Teammate implemented; JWT stayed only for short-lived handshake tokens (signup, phone verification).
- **R**: Simpler auth path; single-instance scale made stateless-ness a non-requirement. The dead RefreshTokenService in the repo is the fossil of that pivot.
- **꼬리질문 대비**:
  - *"When WOULD you use JWT?"* → 다중 서비스/제3자 검증, 수평 확장에서 세션 스토어가 병목일 때, 짧은 TTL 위임 토큰. 우리도 핸드셰이크 토큰(SIGNUP/PHONE_VERIFY)은 JWT 유지 — 무상태가 실제로 이득인 지점.
  - *"Session scale-out?"* → 현재 in-memory Tomcat 세션 = 단일 인스턴스 전제. 스케일아웃 시 spring-session-data-redis 1의존성 추가로 해결 — 전환 비용이 낮다는 것까지 알고 내린 결정이라 말할 것.
  - *"CSRF with cookie sessions?"* → 정직 존: CSRF 비활성 + SameSite 미설정 상태였음을 인지. 개선안(SameSite=Lax 명시, CSRF 토큰 또는 더블서브밋) 즉답 준비.
  - 위험 존: 시큐리티 필터 체인 세부 구현은 팀원 영역 — "구현 리뷰는 했지만 작성자는 팀원" 선 지키기.

## S3. TSID와 JS 53-bit 사고 (— "Tell me about a production bug")

- **S**: Moved PKs from per-table sequences (allocationSize=1, a round-trip per INSERT) to application-generated TSIDs — time-sortable 64-bit IDs. Compared against UUID (v4 index fragmentation, 128-bit size) in a team doc.
- **T**: Two days later the frontend (Next.js) silently truncated IDs — JS numbers are safe only to 2^53-1.
- **A**: Same-day fix: every response DTO serializes IDs as strings (33 files). I even asked in the PR "is this really what industry does?" — it is (Twitter's id_str precedent).
- **R**: Rule stuck for all future DTOs. Bonus thread: 실무자 피드백 기반 FK 제거 — "DB 레벨 완화, 코드 레벨 강화" (예방적 운영 유연성 결정, studio 경계 14개 드롭).
- **꼬리질문**: *"TSID vs UUIDv7?"* → **reference-tsid-vs-uuid.md의 한 단락 버전 암기** (핵심: v7은 정렬은 풀지만 16B — FK-less 설계라 ID 폭이 모든 참조 컬럼×인덱스에 곱해짐 + 채택 시점 pg 17엔 v7 네이티브 없음 + "pg 18이면 재비교하겠다"로 마무리); *"FK 없이 정합성은?"* → 애플리케이션 서비스 레벨 검증 + 소프트 딜리트 전략 + partial unique index(`WHERE deleted_at IS NULL`)로 재가입 유니크 해결; *"고아 행 리스크?"* → 인정 + 배치 정합성 검사 개선안.

## S4. 지리공간 검색 (— "Walk me through a system you built")

- 아키텍처 요점: 뷰포트 = `st_intersects(location, st_makeenvelope(...))`를 QueryDSL 템플릿으로 주입(13개 동적 필터와 단일 쿼리 합성), 2단계 ID→엔티티 쿼리 + countDistinct, 정렬 화이트리스트 2종.
- 수치 존: 스튜디오 130건 — **"인덱스를 안 만든 게 아니라 측정상 불필요해 보류"** (pg_indexes 실측 확인). 성장 시 GiST 1줄.
- 진화 서사: 화장실 필터 3단 진화(도메인 순수성 vs API 사용성 왕복), AI 코드 리뷰 반영으로 키워드 검색 IN→EXISTS 리팩토링.
- 거리 일화(S1과 연결): 유료 Directions API가 페이지당 N회 → 핫픽스로 제거, Haversine 자체 계산. 공공 주소 API는 **DB 저장 허용을 유선으로 확인**하고 채택(컴플라이언스 실사).
- 꼬리질문: *"geography vs geometry?"*, *"대규모라면?"* → GiST + 필터 서브쿼리 조인 키 인덱스 + building_info EXISTS 7회 → 조인 1회 통합 + 키워드는 pg_trgm — 개선 로드맵을 즉답.

## S5. Filestorage 정책 엔진 (— 코드 품질/리팩토링 질문용)

- 서사: 단일 버킷 일괄 발급(V0) → PUBLIC/PRIVATE 분리 + temp/ 승격 + trash(PR #50) → 유스케이스별 메서드 증식(중간형) → **enum 정책 테이블 리팩토링 — 기능 추가하며 순감 32라인**. "경로 문자열 하드코딩 금지" 규칙이 실제로 리포 전체 위반 0.
- 설계 포인트: S3에 move가 없어 copy+delete, 앱 정리 + S3 라이프사이클 7일 이중 방어, presigned 직접 업로드로 서버 대역폭 회피(소형 인스턴스 정합).
- 정직 존: S3-DB 원자성 없음(보상 트랜잭션 미구현 — 개선안: 실패 허용 + 라이프사이클 백스톱이 현 답), 콘텐츠타입 검증이 리팩토링 중 소실(교훈: AI 협업 시 diff의 삭제 라인 리뷰).

## S6. 콜드스타트 대책 (— 짧은 보조 스토리)

- 단일 커밋으로 3종 세트: DB 워밍업 후 명시적 `ReadinessState.ACCEPTING_TRAFFIC` 발행하는 WarmupListener + `load-on-startup:1` + spring-context-indexer. 한 달 뒤 ALB 헬스체크를 `/actuator/health/readiness`로 연결 — 워밍업 완료 전 트래픽 미수신 보장.
- 주의: 수치 미측정 — 숫자 지어내지 말 것. **로컬 근사 측정(2026-08-03, evidence/coldstart-local-measurement.md)에서 워밍업 유무 차이가 오차 범위**로 확인됨 → 이 대책의 본질은 지연 개선이 아니라 **정합성**: "ALB가 준비 완료 전 트래픽을 보내지 않도록 명시적 readiness 게이팅" 프레이밍으로 말할 것. 꼬리질문 *"측정했나?"* → "로컬에선 재현 안 됨을 확인했고, 프로드 요인(버스터블 CPU·원격 풀 초기화·LB 타이밍)이 지배적이라 판단" — 측정해 봤다는 사실 자체가 가점.

---

## 정직 존 (선제 인정이 유리한 것들)

| 항목 | 프레이밍 |
|---|---|
| 실사용자 8명, 프리런치 종료 | 숨기지 않기 — "수요 검증 실패에서 배운 것"(공급 130개 선확보 vs 수요 미개화) + 엔지니어링은 남았다 |
| admin API 무보호 | 백오피스 로그인 미구현 단계에서 중단 — 인지된 갭, 첫 조치 계획(URL 규칙 + ADMIN 롤) 즉답 |
| 스케줄러 미실행(@EnableScheduling 부재) | 이번 감사에서 발견 — "정적 분석으로 찾아냈고, 통합 테스트에 스케줄러 등록 검증을 넣었을 것" |
| 테스트 부재 | 속도 우선의 의식적 부채 — 지금이라면 검색 쿼리 빌더(13필터 조합)부터 테스트 |
| draft 미완(승격 경로 없음) | 미완임을 먼저 말하기 — 설계 의도(JSONB 부분 저장)와 남은 작업을 구분해 설명 |

## 금지 존 (주어 오류 방지)

- OAuth 클라이언트/SMS/owner 로그인/도메인 분리 **구현**을 "I built"로 말하지 말 것 → "my teammate implemented, we reviewed together".
- JWT→세션은 "I proposed and designed"까지만. 필터 체인 코드 세부는 리뷰어 관점으로.
- Confluence의 TSID 비교 문서 — 접근 복구 전엔 "we documented the comparison" 수준으로.
