# 10. /projects 카드 콘텐츠 확정 — Muroom

> 2026-08-03 작성. 근거: docs/dossier 01~08 + 실측(evidence/) + 이번 검증(gh/curl).
> 원칙 적용: [확인]/[실측]만 문안에 사용. [증언](기억 기반)은 문안에서 제외하고 표에 사유 명시.

## 1. 팩트 체크 — 현재 카드 vs 실측

| 카드 주장 | 판정 | 근거 / 교정값 |
|---|---|---|
| 배지 "Founder" | ✅ 유지 | **사업자등록상 대표가 Monte 본인** [증언 — 2026-08-03 확정]. 공동 창업이지만 등록 대표이므로 "Founder" 정확·방어 가능 |
| "University-backed" | ✅ 사용 가능 | 대학 창업지원금 수령 [증언 — 2026-08-03 본인 확정]. 지원금은 전액 마케팅비 배정(04 F) |
| "music-studio **rental** platform" | ❌ 불일치 | 예약/대여 도메인이 코드에 존재하지 않음 [확인: 모듈 맵 — reservation/booking 부재]. → **"search platform"** (검색·등록 플랫폼) |
| "team of **six**" | ❌ 교정 | 실제 **5인**: 디자이너 2 · 프론트 1 · 백엔드 2 [증언 — 2026-08-03 확정]. (백엔드 2 = monte+Sehee, git 정합; shlee8 4커밋은 외부 소규모 기여로 해석) |
| "Led backend" | ⚠️ 교체 | 커밋 다수 저자가 팀원이라 방어 취약 → **소유 영역 명시**: 검색·filestorage·인프라·핵심 마이그레이션 5종·배포 전부 monte [확인: 02-history §1] |
| "MVP shipped in **5 months**" | ❌ 불일치 | 첫 커밋 2025-10-19(3260637) → 베타 배포 2025-11-09(1c0075f, Dockerfile+prod 프로파일 포함) = **3주** [확인]. 실사용 소프트런칭은 2025-12 [실측: 12월 검색 549건·가입 14명] = ~7주. "5개월"은 어떤 기점으로도 성립 안 함 → **"beta live in 3 weeks"** |
| "Geocoding → EPSG:5179→WGS84" | ✅ | `map/application/CoordinateTransformService.java:15-19` (proj4j), Juso 2단 호출 [확인] |
| "PostGIS viewport search" | ✅ | `st_intersects(st_makeenvelope(...))` — `StudioRepositoryImpl.java:194-205` [확인] |
| "13 dynamic filters" | ✅ | `studioFilteringWhereClause` — `StudioRepositoryImpl.java:116-138`, 03-geospatial-search §2.4 표 [확인] |
| 스택 칩 Spring Boot·PostGIS·Terraform | ✅ | build.gradle / geography 컬럼+공간 쿼리 / infra*.tf 전체 [확인] |

추가로 카드에 쓸 수 있는 [실측] 수치: 스튜디오 **130**·룸 **1,273**(prod DB), prod+dev 월 **~$150**(청구서, Terraform 이관 후), 커밋 507·PR 94.

## 2. 설명 문안 (검증된 사실만)

**A안 — 지오 파이프라인 전면**
- EN: "Music-studio search platform, co-founded. Owned geospatial backend & infra: geocoding → EPSG:5179→WGS84 → PostGIS viewport search with 13 dynamic filters. 130 studios / 1,273 rooms catalogued; beta live in 3 weeks." (34w)
- KO: "공동 창업한 합주실 검색 플랫폼. 지오코딩 → EPSG:5179→WGS84 → PostGIS 뷰포트 검색과 13개 동적 필터, 인프라 전체 담당. 스튜디오 130개·룸 1,273개 등록, 첫 커밋 3주 만에 베타 배포."

**B안 — 검색+인프라 균형 (추천)**
- EN: "Co-founded studio-search platform; owned search & all AWS infra. PostGIS viewport queries, 13 dynamic filters; Terraform-managed prod + dev at ~$150/mo. Beta shipped 3 weeks after first commit." (30w)
- KO: "공동 창업한 합주실 검색 플랫폼. 검색과 AWS 인프라 전체 담당 — PostGIS 뷰포트 쿼리·13개 동적 필터, Terraform으로 prod+dev 월 ~$150 운영. 첫 커밋 3주 만에 베타 배포."

**C안 — 구축물 나열형**
- EN: "Seoul music-studio search. Built PostGIS viewport search (13 filters, EPSG:5179→WGS84), S3 file-storage policy engine, Terraform AWS for prod + dev (~$150/mo). 130 studios, 1,273 rooms; beta in 3 weeks." (33w)
- KO: "서울 합주실 검색 서비스. PostGIS 뷰포트 검색(13개 필터, EPSG:5179→WGS84), S3 파일 정책 엔진, Terraform 기반 prod+dev AWS(월 ~$150) 구축. 스튜디오 130개·룸 1,273개, 3주 만에 베타."

**추천: B안** — 소유권 주장이 전부 방어 가능("owned search & all AWS infra"는 커밋 저자로 실증)하면서 기술 차별화(PostGIS)·운영 수치($150)·속도(3주)를 한 카드에 담고, 케이스 스터디 1편(비용 회고)과 자연스럽게 이어짐.

## 3. 스택 칩

**현행 유지: `Spring Boot · PostGIS · Terraform`** — 앱/공간DB/IaC의 삼각 구도가 3초 스캔에 최적. 검토한 대체 후보와 탈락 사유: QueryDSL(Spring 생태계에 포함으로 읽힘), Valkey/Redis(보조적), AWS(Terraform이 더 구체적이고 강한 신호), Flyway(차별성 낮음).

## 4. 링크

**GitHub** — `https://github.com/muroom-studio/muroom-backend-bach` — **현재 PRIVATE** [확인: gh repo view]. (org의 muroom-frontend-handel/beta/admin은 PUBLIC.)
- 선택지: ① 공개 전환 ② README 미러 ③ org/프로필 링크 유지
- **추천: 8월 리소스 해체 + 외부 키 폐기 후 공개 전환.** 근거: `MusicianAuthControllerDocs.java:49,73`에 카카오 REST 키·구글 client ID가 하드코딩되어 git 히스토리에 존재 [확인 — raw/auth-subsystem.md]. 서비스 종료로 키를 전부 폐기하면 노출 위험이 소멸하므로 히스토리 재작성 없이 공개 가능. `terraform.tfvars`는 미추적 확인됨 [확인]. 공개 전 최종 시크릿 스캔(gitleaks 등) 1회 권장. 그 전까지 카드는 GitHub 링크 보류 또는 org 링크.

**Case study** — 블로그에 Muroom 글 **미발행** [확인: /api/search 결과는 테스트 글 3건뿐]. blog/en/01(비용 회고)을 발행하면 그 slug로 확정 — slug 확정: `/writing/muroom-aws-on-pocket-money` (시리즈: muroom-deleting-jwt, muroom-ids-javascript). 발행 전까지 카드의 Case study 링크는 비활성 처리 권장.

## 5. 최종 카드 (조립본 — 2026-08-03 증언 반영 확정)

- 배지: **Founder** (사업자등록 대표 [증언])
- EN: *University-backed music-studio search platform, founded in a team of five. Owned backend search & all AWS infra: PostGIS viewport queries, 13 dynamic filters; Terraform prod + dev at ~$150/mo. Beta live in 3 weeks.* (35w)
- KO: *대학 창업지원을 받은 합주실 검색 플랫폼, 5인 팀 창업(대표). 백엔드 검색과 AWS 인프라 전체 담당 — PostGIS 뷰포트 쿼리·13개 동적 필터, Terraform으로 prod+dev 월 ~$150 운영. 3주 만에 베타 배포.*
- 칩: `Spring Boot` `PostGIS` `Terraform`
- 링크: Case study → `/writing/muroom-aws-on-pocket-money` (slug 확정 — 본인 커밋 dda059d 기준) · GitHub → `github.com/muroom-studio/muroom-backend-bach` (공개 전환 후 활성화)

변경 이력: "rental→search", "6인→5인", "5개월→3주"는 실측·증언 우선 원칙으로 교정. "Founder"·"University-backed"는 2026-08-03 본인 확정 증언으로 유지/복원.
