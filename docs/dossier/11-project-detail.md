# 11. /projects/muroom 상세 페이지 콘텐츠 패키지

> 2026-08-04 작성. 근거 태그: [확인]=코드/커밋, [실측]=DB/AWS/사이트, [증언]=Monte 확정 답변.
> 카드(10-projects-card.md) → **본 상세 페이지** → 케이스 스터디 3편(/writing/muroom-*) → GitHub 구조의 가운데 층.

---

## 1. 원라이너

- EN: **Map-based studio search for Seoul musicians — founded, built, and wound down in ten months.** (15w)
- KO: **서울 뮤지션을 위한 지도 기반 합주실 검색 — 창업부터 종료까지 10개월.**

## 2. 스탯 스트립 (5개 선정)

| 수치 | 라벨 EN / KO | 근거 |
|---|---|---|
| **130** | studios catalogued / 등록 스튜디오 | [실측: prod DB 2026-08-02] |
| **1,273** | rooms listed / 등록 룸 | [실측: prod DB] |
| **110** | owners cold-called / 콜드콜로 확보한 사장님 | [증언 Q25 + DB owners 110 정합] |
| **3 weeks** | first commit → beta / 첫 커밋에서 베타까지 | [확인: 3260637(10-19) → 1c0075f(11-09)] |
| **~$150/mo** | prod + dev on AWS / 2환경 운영비 | [실측: Cost Explorer, evidence/] |

- 선정 기준: 공급 실적 2개(규모) + 영업 1개(창업 신호) + 속도 1개 + 비용 1개 — 서로 다른 축 5개가 3초에 "실제로 만들고 운영했다"를 전달. 제외: 507 commits·94 PRs(GitHub이 대신 말해줌 — 카드 중복 회피), 13 filters(§8과 케이스 스터디가 다룸).

## 3. 제품 소개 (엔지니어 톤)

**EN** (3문단):

> Muroom is a map-first search service for music practice studios in Seoul. Musicians pan a map; every studio inside the viewport appears as a price-tagged marker and as a card in a synced list, filterable across thirteen dimensions — price, room size, floor type, parking, lodging, fire insurance, forbidden instruments, and per-category amenities. Each listing shows rooms with per-room pricing, building facts, and straight-line distances to up to three nearby subway stations.
>
> The supply side was operations, not self-serve: we cold-called studio owners, collected their floor plans and room specs, and registered 130 studios (1,273 rooms) through an internal admin flow. Owner accounts exist for every studio, but owner-facing self-registration was still being built when the project wound down — an eight-step wizard with JSONB-backed draft saves was the last feature in flight.
>
> Under the hood it's a deliberately boring stack doing a few interesting things: one Spring Boot monolith, PostgreSQL with PostGIS for viewport queries, addresses geocoded through Korea's public road-address API (with an EPSG:5179→WGS84 transform), sessions over JWT, and the whole AWS footprint defined in Terraform at ~$150/month for two environments.

**KO**:

> Muroom은 서울의 합주실·연습실을 지도로 찾는 검색 서비스입니다. 지도를 움직이면 뷰포트 안의 스튜디오가 가격 마커와 동기화된 리스트 카드로 나타나고, 가격·룸 크기·층 유형·주차·숙박·화재보험·금지 악기·카테고리별 옵션까지 13개 차원으로 필터링됩니다. 각 스튜디오는 룸별 가격, 건물 정보, 인근 지하철역 최대 3곳까지의 직선거리를 보여줍니다.
>
> 공급은 셀프서브가 아니라 운영으로 만들었습니다. 사장님들에게 콜드콜을 돌리고 도면과 룸 스펙을 받아 내부 관리자 플로우로 130개 스튜디오(룸 1,273개)를 직접 등록했습니다. 사장님 셀프 등록은 프로젝트 종료 시점에 개발 중이던 마지막 기능으로, JSONB 임시저장을 붙인 8단계 위저드였습니다.
>
> 내부는 의도적으로 지루한 스택이 몇 가지 흥미로운 일을 하는 구조입니다. Spring Boot 모놀리스 하나, 뷰포트 쿼리를 위한 PostgreSQL+PostGIS, 공공 도로명주소 API 지오코딩(EPSG:5179→WGS84 변환), JWT 대신 세션, 그리고 Terraform으로 정의된 월 ~$150의 2환경 AWS.

## 4. 아키텍처

**개요 (EN)**: A single Spring Boot service on ECS (EC2, Graviton) behind one ALB that host-routes prod and dev; data lives on self-managed EC2 instances — PostgreSQL 17 + PostGIS and Valkey — with images uploaded browser-direct to S3 via presigned URLs. Credentials rotate through a Secrets Manager Lambda that the app survives without redeploys, and all egress rides a single t4g.nano NAT instance. Everything below the DNS line is Terraform. [확인: infra/*.tf 전수, 01-architecture]

**개요 (KO)**: 단일 Spring Boot 서비스가 ECS(EC2, Graviton) 위에서 돌고, ALB 하나가 호스트 헤더로 prod/dev를 라우팅합니다. 데이터 계층은 자체 운영 EC2 — PostgreSQL 17+PostGIS, Valkey — 이고 이미지는 presigned URL로 브라우저에서 S3에 직접 업로드됩니다. 자격증명은 Secrets Manager 로테이션 Lambda로 갱신되며 앱은 재배포 없이 이를 견디고, 모든 아웃바운드는 t4g.nano NAT 인스턴스 하나를 지납니다. DNS 아래 전부가 Terraform입니다.

**다이어그램 스펙** (디자이너용 — 노드/엣지/레이블, 전부 [확인]):

```
[영역: Internet]
  N1 Browser (Next.js on Vercel)

[영역: AWS VPC 10.0.0.0/16 — Terraform]
  N2 ALB — label: "host routing · api / dev-api · default 404"
  N3 ECS on EC2 (Spring Boot) — label: "t4g.medium prod / t4g.small dev"
  N4 PostgreSQL 17 + PostGIS (EC2 t4g.small) — label: "viewport queries · st_intersects"
  N5 Valkey 8.1 (EC2 t4g.micro) — label: "favorites · SMS rate-limit"
  N6 NAT instance (t4g.nano) — label: "all egress · nftables"
  N7 Secrets Manager + rotation Lambda — label: "prod 7d · dev 1d"

[영역: AWS 리전 서비스]
  N8 S3 public/private buckets — label: "presigned direct upload · temp/ → permanent/"
  N9 S3 backup buckets — label: "pg_dump nightly · WAL every 10s · RDB"

[영역: External APIs]
  N10 Juso road-address API — label: "geocoding · EPSG:5179→WGS84"
  N11 Seoul Open Data — label: "subway master sync"

엣지:
  N1→N2 "HTTPS"        N2→N3 "8080 (awsvpc)"
  N3→N4 "JDBC (aws-wrapper + Secrets plugin)"   N3→N5 "Lettuce"
  N1→N8 "presigned PUT/GET (browser-direct)"    N3→N8 "presign 발급/키 관리"
  N7→N4 "rotate"       N3→N7 "re-fetch on auth failure"
  N3→N6→N10, N3→N6→N11 "egress via NAT"
  N4→N9 "WAL 10s + nightly dump"
접근: 운영자→전 인스턴스 SSM Session Manager (SSH/키페어 없음)
```

## 5. 스택 그룹핑

| 그룹 | 항목 |
|---|---|
| **App** | Java 21 · Spring Boot 3.5 · Spring Security (session) · JPA/Hibernate + hibernate-spatial · QueryDSL 5 · OpenFeign · springdoc(OpenAPI) |
| **Data** | PostgreSQL 17 · PostGIS · Flyway · Valkey (Redis protocol) · TSID (64-bit app-side PK) · JSONB (draft/신고 스냅샷) · proj4j (EPSG:5179→WGS84) |
| **Infra** | Terraform · AWS — EC2 (Graviton t4g) · ECS · ALB · S3 (presigned) · Secrets Manager + rotation Lambda · CloudWatch · DLM · NAT instance · Docker (multi-stage, non-root) |
| **Ops** | SSM Session Manager (no SSH) · actuator readiness gating (ALB) · WAL→S3 10초 쉬핑 (준-PITR) · calendar-versioned 수동 배포 (`prod-vYY.MM.DD.X`) · p6spy · AI 코드 리뷰 (Gemini, PR 템플릿 내장) |

전부 [확인: build.gradle / infra/*.tf / .github/pull_request_template.md]

## 6. 역할 분담

**EN**: Five-person team: two designers, one frontend developer (Next.js), two on the backend. I founded the company (registered CEO — Taehwan "Monte" Kim) and owned the geospatial search, the studio domain and its file-storage engine, every schema migration, all AWS infrastructure and Terraform, and every deploy. My backend teammate built authentication (OAuth, sessions, SMS verification) and the member-facing domains — my-page, terms, reports, inquiries. Architecture rules — the layer structure, FK-less references, ID strategy — were mine; the switch from JWT to sessions was my call, implemented by my teammate.

**KO**: 5인 팀 — 디자이너 2, 프론트엔드 1(Next.js), 백엔드 2. 저는 창업자(사업자등록 대표, 김태환)로서 지리공간 검색, 스튜디오 도메인과 파일 스토리지 엔진, 모든 스키마 마이그레이션, AWS 인프라와 Terraform 전체, 그리고 모든 배포를 맡았습니다. 백엔드 팀원은 인증(OAuth·세션·SMS 인증)과 회원향 도메인 — 마이페이지·약관·신고·문의 — 을 구축했습니다. 레이어 구조·FK 없는 참조·ID 전략 같은 아키텍처 규칙은 제가 정했고, JWT→세션 전환은 제가 결정하고 팀원이 구현했습니다.

근거: 저자 귀속 [확인: 02-history §1 — monte 188커밋/핵심 마이그레이션 5종/deploy 전부], 팀 구성·대표 [증언 + 사이트 푸터 "(주) 뮤룸 대표이사" [실측: muroom.kr footer]], JWT→세션 결정 [증언 Q30].

## 7. 타임라인 (마일스톤 7개)

| 날짜 | 마일스톤 EN / KO | 근거 |
|---|---|---|
| 2025-10-19 | First commit / 첫 커밋 | [확인: 3260637] |
| 2025-11-09 | Beta live on AWS (3 weeks in) / 베타 배포 | [확인: 1c0075f — Dockerfile+prod 프로파일 포함] |
| 2025-12 | Soft launch — first real users / 소프트 런칭 | [실측: 12월 검색 549건·가입 14명] |
| 2026-02-03~04 | Terraform migration + RDS→self-managed EC2 / IaC 이관 | [확인: 43be918, PR #89] |
| 2026-02-22 | Last production deploy / 마지막 배포 | [확인: a65ae35 deploy: dev-3594f6b] |
| 2026-05 | Operations paused, development continued / 운영 중단 | [증언 Q33 + 실측: ECS 인스턴스 5/10 교체] |
| 2026-08 | Wound down; credits exhausted / 서비스 종료 | [증언 Q34 + 실측: 크레딧 소진 곡선] |

## 8. 의사결정 하이라이트 (5개)

1. **RDS → self-managed Postgres on EC2** — Server bills were coming out of our own pockets, so I traded managed guarantees for ~$150/mo across two environments — then rebuilt the guarantees by hand: 3-layer backups, WAL shipping every 10s, and RDS-grade credential rotation on a database AWS doesn't manage. / 서버비가 사비였기에 관리형 보증을 포기하고 2환경 월 ~$150로 — 그리고 그 보증을 손으로 재구축했습니다(3중 백업, 10초 WAL 쉬핑, 자체 DB 위의 RDS급 로테이션). → [/writing/muroom-aws-on-pocket-money]
2. **Walking time → straight-line distance** — The paid directions API billed per studio per page; the free public alternative was rate-limited to unusability. We deleted the feature and compute Haversine in Java: strictly worse information, strictly zero marginal cost. / 유료 길찾기는 페이지당 과금, 무료 공공 API는 쿼터로 불가 — 기능을 지우고 Java Haversine으로. 정보는 후퇴, 한계비용은 0. → [/writing/muroom-aws-on-pocket-money]
3. **JWT → sessions** — Once refresh-token revocation forced state into Redis, JWT was sessions with extra steps. I proposed the switch; JWT survives only as short-lived handshake tokens. / 리프레시 무효화가 Redis 상태를 요구하는 순간 JWT는 "단계 많은 세션" — 전환을 제안했고, JWT는 단기 핸드셰이크 토큰으로만 남았습니다. → [/writing/muroom-deleting-jwt]
4. **Sequences → TSID, IDs as strings** — App-generated 64-bit time-sortable PKs killed a per-INSERT round-trip; two days later JavaScript's 2⁵³ limit silently truncated them, and every API ID became a string the same afternoon. / 앱 생성 64비트 시간 정렬 PK로 INSERT 왕복 제거 — 이틀 뒤 JS의 2⁵³ 한계가 ID를 조용히 자르며, 그날 오후 모든 API ID가 문자열이 됐습니다. → [/writing/muroom-ids-javascript]
5. **Dropping foreign keys (studio domain)** — On working-engineer advice: relax the database, enforce in code. Fourteen FKs went; creation-time existence checks and service-owned cascade deletes took their place, with partial unique indexes handling soft-delete uniqueness. / 실무자 조언에 따라 "DB는 완화, 코드는 강제" — FK 14개를 걷어내고 생성 시점 존재 검증·서비스 소유 연쇄 삭제·partial unique index로 대체. → [/writing/muroom-ids-javascript]

(6안 후보였던 draft JSONB 저장은 링크할 글이 없어 제외 — 케이스 스터디 미작성. 필요 시 "링크 없음"으로 추가 가능.)

## 9. 링크 · 상태

- **GitHub**: 공개 전환 전까지 org 링크 → `https://github.com/muroom-studio` (프론트 3개 리포 공개 상태). 백엔드 공개 전환 후 `muroom-studio/muroom-backend-bach`로 교체 [실측: 현재 PRIVATE].
- **Case studies**: `/writing/muroom-aws-on-pocket-money` · `/writing/muroom-deleting-jwt` · `/writing/muroom-ids-javascript` (slug 확정, 발행 대기)
- **서비스 상태 표기 제안**: 배지형 — EN `● Wound down · Aug 2026` / KO `● 2026년 8월 서비스 종료`. 톤 근거: "실패를 숨기지 않는 포스트모템 포트폴리오"가 시리즈 전체 컨셉이므로 상세 페이지도 종료를 배지로 정면 표기. 라이브 링크는 종료 후 제거하고 스크린샷으로 대체.

## 스크린샷 인벤토리 (assets/ — 2026-08-04 라이브 캡처, 2880×1800 @2x)

| 파일 | 캡션 EN / KO |
|---|---|
| `01-map-search-list.png` | Viewport search: price markers synced with the result list, nearest-station distance per card / 뷰포트 검색 — 가격 마커와 동기화된 리스트, 카드마다 인근 역 거리 |
| `02-filters-options.png` | Amenity filter panel — shared vs. per-room options (the schema's COMMON/INDIVIDUAL categories) / 옵션 필터 패널 — 공용/개인 구분(스키마의 COMMON/INDIVIDUAL 그대로) |
| `02b-filters-building.png` | Building-type filter chip open / 건물 유형 필터 |
| `03-studio-detail.png` | Studio detail: photos, three nearby stations with straight-line distances, contact CTAs / 스튜디오 상세 — 사진, 인근 역 3곳 직선거리, 문의 CTA |
| `03b-studio-detail-building.png` | Building-info tab: floor, parking, restroom, fire insurance — owner-entered facts / 건물정보 탭 — 사장님이 등록하는 정보(지층·주차·화장실·화재보험) |
| `03c-studio-detail-rooms.png` | Rooms tab: room count, forbidden instruments, photos, availability state / 방정보 탭 — 방 개수·금지악기·사진·계약 상태 |
| `04-studio-boast.png` | UGC feed, empty state — feature was retired (see note) / 작업실 자랑 피드(빈 상태 — 기능 회수, 아래 참조) |

## 질문 → 답변 반영 완료 (2026-08-04)

1. **사장님 등록 플로우** [해소]: 프론트 화면은 구현 중단으로 **존재하지 않음** [증언]. API 현황 [확인]: ① owner **계정 가입** API는 존재(`POST /api/v1/owners/register`, SMS 인증 연동 — 단 SecurityConfig permitAll 미등록이라 비로그인 호출이 막힌 상태) ② **스튜디오 등록**은 admin API(`/api/admin/studios`) 전용 ③ owner용 8단계 위저드 draft API는 feature/studio 브랜치에만 존재, 미배포. → 캡처 불가 확정, 상세 페이지 §3의 "still being built" 서술과 정합.
2. **자랑 피드** [해소·증언]: 기능은 실제 배포됐으나 사용 저조(게시물 2건)로 **기획자가 "유지 시 브랜드에 마이너스" 판단 → 화면에서 기능 회수**. DB의 2건은 그 잔존. 과거 캡처본 수색은 **불필요 권고** — 상세 페이지 가치가 낮고, "만들었고, 데이터를 보고, 내렸다"는 제품 결정 서사는 문서 기록(04-decisions F)으로 충분. 빈 상태 캡처는 각주용으로만 유지.
3. **03b 검수** [해소]: 초판은 휠이 리스트를 스크롤한 오캡처였음 → 삭제 후 **건물정보 탭·방정보 탭 재캡처로 교체**(위 인벤토리). 룸 보유 스튜디오(미라클사운드)로 대상 변경.
4. **실명 표기** [확정]: 표기한다 — §6에 반영. 공개 근거: 사이트 푸터 "(주) 뮤룸 대표이사 김태환" [실측].
