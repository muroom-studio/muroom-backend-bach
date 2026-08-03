# 06. 완전성 점검 — 무엇을 읽었고, 무엇을 안 읽었나

> 작성일: 2026-08-02 · 목적: 이 도시에의 신뢰 경계를 명시. 07(블로그)·08(면접) 작성 시 이 문서의 "낮은 신뢰도" 항목을 단정적으로 쓰지 말 것.

## 1. 커버된 것 (높은 신뢰도)

| 영역 | 커버리지 | 방법 |
|---|---|---|
| 빌드/설정 (build.gradle, application*.yml, docker-compose, Dockerfile) | 전체 | 직접 정독 |
| Monte 소유 모듈 (studio, studioboasting 요약, filestorage, map, subway, search, common) | 코드 수준 | 에이전트 정독 + 핵심 주장 재검증 |
| 인프라 (infra/*.tf 전체 + user-data 템플릿) | 전체 | 에이전트 정독 |
| 데이터 모델 (엔티티 34개 전수 + 마이그레이션 32개 전수) | 전체 | 에이전트 정독 + init.sql 대조 |
| auth 모듈 (Sehee 영역) | 코드 수준 | 에이전트 정독 — 단 "맥락 전용" |
| git 히스토리 | develop 503커밋 aggregate 전수 + 시그널 커밋 ~40개 diff 판독 | 직접 + 에이전트 |
| PR #1~#94 | 메타데이터(저자·제목·머지일) 전수 + Monte 핵심 PR 10개 본문 | gh CLI |
| 저자 귀속 | 모듈×저자 churn 전수 + 핵심 마이그레이션 5개 저자 확인 | git numstat/log |

## 2. 읽지 않은 / 얕게 본 것

- **Sehee 모듈 내부 상세**: report/inquiry/terms/faq/withdrawal/sms/musician/owner의 서비스 로직 — 의도적 제외(맥락 전용 방침). admin 모듈도 컨트롤러 표면만.
- **PR 리뷰 코멘트**: Gemini Code Assist 리뷰 코멘트 및 상호 리뷰 내용 전량 미수집. "코드 리뷰 반영" 커밋들(8345291 등)로 존재만 확인. → 필요 시 `gh pr view <n> --comments`.
- **Sehee 311커밋의 개별 diff**: aggregate로만 파악. JWT→세션 전환의 정확한 시점·경위는 커밋 단위 미추적.
- **shlee8의 4커밋**: 미조회 (2026-02, favorite/auth 소규모).
- **valkey/valkey.conf·users.acl 상세**, `src/test/`(테스트가 거의 없음 자체는 확인 — Studio/MapSearch 테스트 0), `src/main/resources/static·templates`.
- **미머지 브랜치**: `git branch -a` 확인 완료 — 공공데이터 보행 API 실험 브랜치는 **없음**(증언의 "로컬 폐기" 정합). 잔존 브랜치: `origin/infra`, `origin/temp-infra`(Terraform 작업 잔재로 추정, 내용 미조회), `origin/feature/login-debug`(Sehee 영역 추정). 로컬 stash 1건("WIP on develop: 관리자 대시보드 CORS") — 내용 미조회.
- **런타임 검증 일체**: 앱을 실행하지 않음. 아래 §3의 정적 분석 판정들이 이에 해당.
- **AWS 실계정 상태**: ✅ 2026-08-02~03 읽기 전용 실사(계정 193013155492, profile `monte-muroom`). 확인됨: EC2 8대 인벤토리(Terraform 정합), ALB 트래픽(월 4.4k~13k), 비용 전체 이력(`evidence/cost-by-service-monthly.json` — **2025-11 $98 → 12월 $221 → 1월 피크 $236(RDS 수동 시대) → Terraform 이관 후 $149~158**, 전액 크레딧 상쇄), ALB 일별 트래픽(`evidence/alb-requests-daily.json`).
- **드리프트(누적 5건)**: ① 고아 RDS `muroom-postgres`(2025-11-06, 연결 0, 월 $21) — **최종 수동 스냅샷 `muroom-postgres-final-archive-20260803` 생성 완료**(인스턴스 삭제 후에도 영구 보존, beta_* 확인은 스냅샷 복원으로 가능) ② `temp` t4g.micro(2/4) ③ prod ECS 인스턴스 5/10 교체 ④ **구 수동 시대 VPC `muroom-bach-vpc` 전체 잔존**(동일 CIDR 10.0.0.0/16 — 피어링 불가, RDS 서브넷 기본 라우트는 블랙홀) ⑤ 구 RDS SG가 5432를 0.0.0.0/0에 개방(퍼블릭 접근은 꺼져 있어 VPC 내부 한정 — 수동 시대의 러프한 설정 흔적).
- **부수 확인**: 로테이션 Lambda가 휴면 서비스에서도 여전히 가동 중(prod 7/29, dev 8/2 시크릿 갱신) — "자동화는 서비스가 죽어도 돈다"는 소재. 구 시크릿 3종(muroom-bach-*) 잔존.
- 미확인 잔여: 구 RDS 내부 beta_* 실데이터(스냅샷으로 보존됨 — 필요 시 복원 조회), Route53/ACM 상세.

## 3. 신뢰도 낮은 항목 (단정 금지 목록)

| 주장 | 신뢰도 | 이유 / 검증 방법 |
|---|---|---|
| "JwtAuthenticationFilter는 비기능(Bearer 경로 죽어 있음)" | 중 | 정적 분석만 — 서블릿 필터 등록 순서의 런타임 확인 없음. 검증: 로컬 기동 + Bearer 요청 |
| "스케줄러 미실행(@EnableScheduling 부재)" | 중상 | grep은 확실하나 런타임 미확인. 검증: 로컬 기동 로그 or dev DB에서 만료 draft 잔존 확인 |
| "admin API 무보호" | 중상 | 코드상 확실 — 단 인프라 레벨 방어(예: 콘솔 수동 WAF) 가능성 잔존. Q7로 확인 |
| ~~"GiST 인덱스 없음 → 시퀀셜 스캔"~~ | **확정** | 2026-08-02 prod pg_indexes 실측 — 공간 인덱스 없음. 단 130건 규모라 무영향. 대신 마이그레이션에 없는 수동 유니크 2개(unq_studios_name_*) 발견 — 드리프트 |
| 서비스 활성도 | **확정** | DB 실측: 뮤지션 19(활성 10), 검색 로그 2025-12에 549건이 전부, 30일 활성 0, 스튜디오 130·룸 1,273(공급측 입력). AWS 실측과 교차 완료 |
| 비용 수치 전부 (NAT $3/mo, ALB $18/mo, 월 $50~80 등) | 낮 | 추정치 — 실청구액으로 교체 필요. Q19 |
| "Terraform 이전 = 콘솔 수동 EC2+Docker" | 낮 | 정황뿐. Q5 |
| "beta 서비스 실오픈" | 낮 | 스키마 존재만 근거. Q25 |
| 공공데이터 보행 API 단계 | 증언 | 코드 무흔적 — 명칭·시점 미상. Q6 |
| "소프트 딜리트가 FK 드롭의 직접 동기" | 추론(강) | 한 커밋 동반이 근거. Q1 |
| Sehee 영역 전반의 "왜" | 낮 | 본인 확인 불가 대상 — 서술 자체를 피할 것 |

## 4. 산출물 인벤토리

```
docs/dossier/
├── 01-architecture.md          # 아키텍처 맵 + 요청 흐름 3개 + 스토리 씨앗 7
├── 02-history.md               # 저자 귀속 + 월별 타임라인 + 시그널 색인
├── 03-geospatial-search.md     # 딥다이브 ① (307줄, 증언 반영)
├── 03-filestorage.md           # 딥다이브 ② (169줄)
├── 03-infrastructure.md        # 딥다이브 ③ (167줄)
├── 03-data-layer-decisions.md  # 딥다이브 ④ (139줄)
├── 04-decisions.md             # 결정 17건 + 연결 다이어그램 (증언 반영)
├── 05-questions.md             # 질문 30개 (T1 필수 8 / T2 검증 12 / T3 보조 10)
├── 06-coverage.md              # 본 문서
└── raw/                        # 에이전트 원본 5개 (딥다이브 원천, 파일:라인 근거)
```

## 5. 다음 단계

1. **Monte가 05에 답변** (맞다/틀리다 + 한 줄 보정이면 충분. T1 8개만 먼저 해도 07 진행 가능)
2. 답변을 04에 통합, [추론]→[증언] 승격
3. `07-case-study-draft.md` — 영문 블로그 초안 (문제→제약→결정→결과, 5분 독해). 후보 주제: ① "Running prod+dev on a shoestring" (인프라 비용 최적화) ② "When both the paid and free APIs fail you" (거리 계산 3단 서사) ③ "Two failed attempts and a policy engine" (filestorage 진화)
4. `08-interview-prep.md` — STAR 구조 + 예상 꼬리질문. 저자 원칙: 구현 주어는 Monte 영역만, 팀 성과는 We.
