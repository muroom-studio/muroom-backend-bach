# 12. 블로그 총괄 세션 핸드오프 — Muroom 콘텐츠 패키지

> 2026-08-04. 이 문서 하나로 블로그 반영에 필요한 전부를 파악할 수 있게 정리.
> 위치: `muroom-studio/muroom-backend-bach` 리포, **`docs/dossier` 브랜치** (develop 미머지, 백엔드 리포는 현재 PRIVATE).

## 1. 발행 대기 자산 (전부 최종본)

| 자산 | 파일 | 상태 |
|---|---|---|
| 블로그 글 EN 6편 | `blog/en/01`~`06` | 발행 준비 완료 — 본문 내 상호 링크가 이미 확정 slug 기준 |
| 블로그 글 KO 6편 | `blog/ko/` 동일 구조 | 〃 (직역 아닌 국문 재구성판) |
| /projects 카드 | `10-projects-card.md` **§5 조립본** | 배지·EN/KO 문안·칩·링크 확정 |
| /projects/muroom 상세 | `11-project-detail.md` | 섹션 1~9 EN/KO 쌍 + 아키텍처 다이어그램 스펙(디자이너용) + 캡션 |
| 스크린샷 7장 | `assets/*.png` (2880×1800 @2x) | 라이브 캡처 완료(2026-08-04) — 서비스 종료 후에도 사용 가능 |

**확정 slug** (6편): 회고 시리즈 — `/writing/muroom-aws-on-pocket-money` · `/writing/muroom-deleting-jwt` · `/writing/muroom-ids-javascript` / 기술 딥다이브 — `/writing/muroom-viewport-search` · `/writing/muroom-credential-rotation` · `/writing/muroom-file-storage-policy`
**발행 순서 권장**: 1편(비용) 먼저 — 카드/상세의 Case study 링크가 1편을 가리킴. 이후 회고 2·3편 → 딥다이브 4~6편 순차. 딥다이브 3편(2026-08-13 추가)은 회고 시리즈와 성격 구분: 4=검색 기술, 5=how-to(검색 유입용), 6=리팩토링 서사.
**추가 집필 백로그** (재료는 dossier에 있음, 미집필): ⑦ 프리런치 포스트모템 캡스톤("130개 공급을 채웠고, 8명이 왔다" — 창업 독자층) ⑧ 콜드스타트 널 리절트("고쳤는데 측정해보니 차이가 없었다 — 진짜 가치는 readiness 게이팅") ⑨ 소프트 딜리트 3종+partial unique index ⑩ 2인 팀의 AI 코드 리뷰 활용기.

## 2. 카드 최종본 (10 §5 그대로)

- 배지 **Founder** / 칩 `Spring Boot` `PostGIS` `Terraform`
- EN: *University-backed music-studio search platform, founded in a team of five. Owned backend search & all AWS infra: PostGIS viewport queries, 13 dynamic filters; Terraform prod + dev at ~$150/mo. Beta live in 3 weeks.*
- KO: *대학 창업지원을 받은 합주실 검색 플랫폼, 5인 팀 창업(대표). 백엔드 검색과 AWS 인프라 전체 담당 — PostGIS 뷰포트 쿼리·13개 동적 필터, Terraform으로 prod+dev 월 ~$150 운영. 3주 만에 베타 배포.*
- 링크: Case study `/writing/muroom-aws-on-pocket-money` · GitHub는 **공개 전환 전까지 org**(`github.com/muroom-studio`)

## 3. 반드시 지킬 원칙 (콘텐츠 수정 시)

1. **수치·주장은 dossier에 근거 태그([확인]/[실측]/[증언])가 있는 것만.** 새 수치 창작 금지. 빠른 참조: 팀 5인(디자이너2·프론트1·백엔드2) / 대표 김태환(Monte) / 스튜디오 130·룸 1,273 / 사장님 콜드콜 110 / 실사용자 8 / 첫 커밋 2025-10-19 → 베타 11-09(3주) / prod+dev 월 ~$150(피크 1월 $236) / 크레딧 $1,000(2026-08 소진) / 13 동적 필터 / FK 드롭 14개 / 마이그레이션 5종 전부 Monte 저자.
2. **저자 구분**: 인증(OAuth·세션·SMS) **구현**과 musician/owner 도메인은 팀원(Sehee) — "I built" 금지, "my teammate implemented". JWT→세션은 "Monte가 결정·설계, 팀원 구현"까지 OK. 상세는 `attribute-work-by-author` 원칙 및 08 금지 존.
3. **정직 톤 유지**: 실사용자 8명·서비스 종료를 숨기지 않는 포스트모템 컨셉이 시리즈 전체 훅. 상태 배지 `● Wound down · Aug 2026`.
4. 콜드스타트 개선 **수치 주장 금지**(로컬 측정 결과 차이 없음 — readiness 게이팅 정합성으로만 서술).

## 4. 미결 사항 (블로그 작업과의 의존성)

| 항목 | 상태 | 블로그 영향 |
|---|---|---|
| 글 3편 발행 | 사용자 실행 대기 | 카드·상세의 Case study 링크 활성화 조건 |
| AWS 리소스 해체 | 2026-08 중 예정 (해체 체크리스트는 요청 시 이 세션이 작성) | 해체 후 라이브 링크 제거 → 스크린샷 대체 |
| 백엔드 리포 공개 전환 | 해체+외부 키 폐기 후 (gitleaks 스캔 + **docs/dossier 브랜치 제거/이관 필수** — 비공개 전제 내용 포함) | 카드 GitHub 링크를 org→리포로 교체 |
| 사장님 등록 화면 | 존재하지 않음(구현 중단) — 캡처 불가 확정 | 상세 §3에 이미 정직 서술됨 |

## 5. 참고 문서 맵 (딥다이브 필요 시)

- 배경/근거 전체: `01-architecture.md` ~ `06-coverage.md`, `raw/`(파일:라인 근거), `evidence/`(청구서·트래픽·측정)
- 의사결정 상세: `04-decisions.md` (17건 + 운영 사실 F)
- 면접 대비: `08-interview-prep.md` (STAR 6종 + 정직/금지 존) — 블로그 톤 조정 시 참조
- TSID 비교 상세판: `reference-tsid-vs-uuid.md` (블로그 3편에 압축본 이미 통합됨)
