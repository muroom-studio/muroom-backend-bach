# 02. 히스토리 고고학 — Muroom Backend (bach)

> 작성일: 2026-08-02 · 근거: `git log develop`(503커밋) + feature/studio(7커밋) + GitHub PR #1~#94 전수 조사
> 표기: **[확인]** = 커밋/PR에서 직접 확인, **[추론]** = 정황 추론

## 1. 저자 구분 (이 문서의 대전제)

| 저자 | 계정 | develop 커밋 수 | 담당 영역 (churn 기준) |
|---|---|---|---|
| **Monte (본인)** | `monte-kim`, `monte` | **188** | **studio(검색·등록·draft), studioboasting, infra(Terraform), subway, map, filestorage, admin, common, beta(초기), db-migration 대부분, search** |
| 팀원 Sehee | `2-say`, `Sehee` | 311 | auth(OAuth·세션), user→musician, owner(로그인/가입), report, inquiry, terms, faq, withdrawal, sms, favorite |
| 팀원 shlee8 | `shlee8` | 4 | auth/favorite 소규모 수정 (2026-02) |

- **[확인]** 모듈별 변경 라인수 상위: studio 6,431(monte) / auth 4,785(sehee) / user 4,662(sehee) / studioboasting 4,350(monte) / infra 4,205(monte).
- **[확인]** 핵심 스키마 마이그레이션 5개 전부 monte-kim 저자: `V25120701__init`(f7e2cb7, Flyway 도입), `V25122403__drop_all_sequences`(46b37e2, TSID 전환), `V26012501__add_soft_delete_to_studio_sub_entities` + `V26012502__drop_studio_foreign_keys`(d887210), `V26030801__create_studio_drafts`(b077677).
- 케이스 스터디/면접 자료에서 다룰 수 있는 Monte 소유 스토리: **지리공간 검색, 스튜디오 도메인, 파일스토리지 정책 엔진, Terraform 인프라, TSID/FK 의사결정, studioboasting**. 인증/세션은 Sehee 작업이므로 "팀 맥락"으로만 언급할 것.

## 2. 타임라인 (월별)

### 2025-10 — 부트스트랩 (monte 3커밋)
- **[확인]** 2025-10-19 최초 커밋 `3260637` (monte). Spring Boot 프로젝트 셋업 + 의존성.

### 2025-11 — 베타 서비스 & 기반 (monte 36 / sehee 33)
- **[확인]** PR #1 (monte, 11-09): **베타 기능(작업실 등록, 문의)** — 지금은 삭제된 `beta` 모듈(1,686라인)과 SQL에만 남은 `beta_*` 테이블 3개의 기원. **같은 커밋(1c0075f)에 Dockerfile + application-dev/prod.yml 포함** → 11월부터 AWS에 뭔가 배포되고 있었음 [확인], 구체 토폴로지는 질문 대상.
- **[확인]** PR #4 (monte, 11-22): common 기반(응답 규격·예외 처리 추정).
- **[확인]** PR #5 (monte, 11-25): **지도 뷰포트 마커/리스트 조회 + 서울 공공데이터 지하철역 동기화** — 지리공간 검색의 시작. PR 본문에 "지하철 동기화를 서버 시작 시 자동 1회 vs 관리자 수동 호출" 고민 기록 [확인] → 최종적으로 수동 admin 엔드포인트 채택.
- PR #2,3 (sehee): user 엔티티/도메인 시작. PR #6,7 (sehee): 로그인.
- **[확인]** PR #10,11 (monte, 11-27~29): 스튜디오 옵션 목록/필터링 조회.

### 2025-12 — 최대 스프린트 (monte 115 / sehee 190, 월 305커밋)
- **[확인]** PR #14 (monte, 12-01): **13개 동적 필터 + 스튜디오명/역명 키워드 검색** ("ALL" 옵션 확장 포함).
- **[확인]** PR #17,18 (monte, 12-01): 지도 가격 정보, 최근 검색 이력(7개)+검색 로그.
- **[확인]** PR #21,22 (monte, 12-02~03): 스튜디오 등록(admin) 및 조회.
- **[확인]** PR #27 (monte, 12-07): 사장님 관리 + **Flyway 도입**(`V25120701__init.sql`) — PR 본문에 "Flyway 잘 적용했는지 검토 요청".
- sehee: Kakao 로그인 대장정 (PR #23,28,29,31,32,33,35, 12-03~10), SMS(#39), **refresh 토큰 추가(#41, 12-14)** — 훗날 데드코드가 되는 JWT 인프라의 기원 [확인].
- **[확인]** PR #40 (monte, 12-14, **hotfix/distance 브랜치**): "스튜디오-지하철역 **도보 시간 제거** 및 직선거리 추가" — 1단계에서 발견한 `MapDirectionService` @Deprecated("비용 문제") + Java Haversine 대체의 커밋 실증. **Kakao Directions API 비용 → 핫픽스로 제거**가 확정 스토리 [확인].
- **[확인]** PR #45 (monte, 12-15): restroomTypes 단일 파라미터 → location/gender 분배 필터.
- **[확인]** 12-22 (04cf4ba, monte): **beta 모듈 삭제 + studioboasting 등장** — 베타 랜딩 기능이 정식 UGC 기능("작업실 자랑" + 인스타그램/이벤트 약관)으로 대체됨. 12-22~29 monte의 boast 스프린트 (PR #50,52,55,56,59,64,65,71).
- **[확인]** PR #50 (monte, 12-24): boast + **"S3 presigned url 개별 제공으로 구조 개선"** — filestorage 정책 엔진의 진화점.
- **[확인]** PR #51 (monte, 12-24): **모든 response DTO의 ID를 Long→String 전환**. 커밋 메시지에 "JS에서 53bit까지만 읽어들여 id값이 누락되는 이슈 해결" [확인 — 1단계 추론의 실증]. PR 본문에 "진짜 이렇게 해야 되나? 실무에서도 이렇게 해?"라는 자문 기록 — 의사결정 로그의 생생한 소재.
- **[확인]** 같은 날 46b37e2 (monte): **TSID 적용 및 DDL 반영** + `V25122403__drop_all_sequences.sql` — DB 시퀀스 → 앱 사이드 TSID 전환.
- sehee: mypage 연작(#47~76), 탈퇴(#46), FAQ(#48), 구글 OAuth(#60), 신고(#57).

### 2026-01 — 리팩터 & 도메인 정리 (monte 8 / sehee 70)
- sehee 주도의 달: **user → musician/owner 도메인 분리**(c642178, 01-02), API URL 복수형화(#74), ErrorCode 네이밍 정비(#79), **owner 패스워드 로그인**(#80~85), 탈퇴 스케줄러(#86).
- **[확인]** PR #88 (monte, 01-25): Studio 수정/삭제 + 같은 커밋 묶음(d887210)에서 **studio 서브엔티티 soft-delete 확장 + studio FK 전면 드롭**(`V26012501/02`) — "FK 없는 설계"가 이 시점에 규칙화됨. 콜드스타트 수정(bcdff79, 01-25)도 이때.

### 2026-02 — 인프라의 달 (monte 26 / sehee 15 / shlee8 4)
- **[확인]** 02-03 43be918 (monte): "**인프라 terraform 1차 적용**" → PR #89 (02-04 머지). 이전까지는 수동 관리 인프라였음 [추론 — Dockerfile/prod yml은 11월부터 존재].
- **[확인]** 02-04 하루에 monte의 fix 연타 9건 + dev 배포 4회: SSM 연결, PostGIS 미적용, Docker 보안, S3 경로, delete protection, **Secrets Manager 로테이션 3연전**(b45f1d1 → 3a4527e → 0c9d536 "3차!!!!!!!!!!!!!!!") → 당일 `prod-v26.02.04.A` 배포. RDS용 SAR 로테이션 Lambda를 자체 운영 PG에 물리는 과정의 실전 기록 [확인].
- **[확인]** 02-17: `2dac014` "ecs prod 이중화 이슈 해결"(distinctInstance+100%/200% 배포가 단일 인스턴스 ASG에서 막힌 문제로 추정 [추론]) + `703d0f5` "session 기반 미인증 시 500 이슈 해결" → `prod-v26.02.17.A`.
- **[확인]** 02-22: `3594f6b` "redis connection fail 이슈 해결" → dev 배포.
- sehee/shlee8: favorite(비회원 찜, #91,92), 로그인 에러(#93), 인증 예외 핸들러 통합(#90).
- **[확인]** deploy 커밋 7건 전부 monte — 수동 배포 오퍼레이터도 Monte.

### 2026-03 — draft & 현재 (sehee 3 / monte는 feature/studio에서)
- sehee: PR #94 (03-10): 비인증 요청 JSESSIONID 생성 방지.
- **[확인]** monte, **feature/studio 브랜치(미머지)**: 781ef5a 로컬 DB 동기화(03-06) → 70a1ef7 **S3 파일 관리 모듈 리팩토링**(03-08 — 현재의 filestorage 정책 엔진 형태) → b077677 **studio 임시저장**(03-09, `V26030801`) → 3151cf4/e859aab 이미지 업로드·정리·만료 스케줄러(03-28).
- 이후 커밋 없음 (03-28 ~ 현재 2026-08) — 4개월 공백. 1단계에서 발견한 미완 상태(@EnableScheduling 부재, draft 승격 미구현, owner 등록 API 부재)와 정합 [확인]. → 질문: 중단 이유(출시? 우선순위? 이직 준비?).

## 3. Monte 작업 궤적 요약 (면접 서사용 골격)

1. **베타 → 정식 전환** (11월): 랜딩용 beta 모듈을 혼자 만들어 배포까지, 이후 12월에 정식 기능(boast)으로 대체하며 스스로 삭제.
2. **지리공간 검색 소유** (11-25~12-15): 뷰포트 마커 조회 → 페이지네이션 리스트 → 13개 동적 필터 → 키워드(역명 포함) → restroom 파생 필터 → 거리 표시(도보시간 → 비용 문제로 Haversine 핫픽스).
3. **데이터 계층 의사결정 3연타** (12-07~01-25, 전부 마이그레이션 저자): Flyway 도입 → TSID 전환(+ID String 응답) → FK 전면 드롭+소프트삭제 확장.
4. **파일스토리지 정책 엔진** (12-24 구조 개선 → 03-08 리팩토링): presigned URL 개별 제공 → enum 정책 테이블(FileStorageLocation) + temp/permanent/trash 라이프사이클.
5. **인프라 IaC 이관과 운영** (02월): Terraform 1차 적용 → 로테이션/PostGIS/SSM 등 실전 트러블슈팅 → 수동 calendar-versioned 배포 체계. 비용 최적화 선택(NAT 인스턴스, 자체 운영 DB/Valkey)의 주체.
6. **studio draft** (03월, 진행 중): JSONB 블롭+TTL 설계, 미완 지점 존재.

## 4. 팀원 작업 (맥락 전용 — 본인 작업으로 서술 금지)

- **Sehee**: 전체 인증 서브시스템 (Kakao/Google OAuth 수동 구현, refresh 토큰 도입 → 이후 세션 전환, owner 패스워드 로그인, SMS 인증), user→musician/owner 도메인 분리 리팩터, mypage/약관/신고/문의/FAQ/탈퇴, API 컨벤션 정비(복수형 URL, ErrorCode 네이밍), favorite(비회원 찜 + Lua 마이그레이션 추정 — 저자 확인 필요 시 개별 커밋 조회).
- 1단계에서 발견한 "JWT→세션 전환" 스토리는 **Sehee 영역**: refresh 토큰 추가(#41, 12-14) → accessToken 임시 처리(#38) → 세션 기반 정착 → 데드코드 잔존. Monte 관점에서는 "팀이 겪은 전환"으로만 언급 가능.

## 5. 시그널 이벤트 색인 (딥다이브·의사결정 로그 연결점)

| 이벤트 | 커밋/PR | 저자 | 연결 |
|---|---|---|---|
| Kakao Directions 제거 → Haversine | PR #40 (hotfix/distance), MapDirectionService @Deprecated | monte | 04-결정 |
| ID Long→String (JS 53bit) | PR #51, 0e6a8ca | monte | 04-결정 |
| TSID 전환 + 시퀀스 드롭 | 46b37e2, V25122403 | monte | 04-결정 |
| FK 전면 드롭 + 소프트삭제 확장 | d887210, V26012501/02, PR #88 | monte | 04-결정 |
| Flyway 도입 | f7e2cb7, PR #27 | monte | 04-결정 |
| presigned URL 구조 개선 | PR #50 | monte | 03-filestorage |
| Terraform 이관 | 43be918, PR #89 | monte | 03-infra |
| Secrets 로테이션 분투 | b45f1d1→0c9d536 (02-04) | monte | 03-infra |
| ECS prod 이중화 | 2dac014 | monte | 03-infra |
| 콜드스타트 해결 | bcdff79 (01-25) | monte | 04-결정 (WarmupListener 세트) |
| beta → studioboasting 대체 | 04cf4ba | monte | 02-히스토리 |
| JWT→세션 전환 | PR #38,41 등 | **sehee** | 맥락 전용 |
| user→musician 분리 | c642178 | **sehee** | 맥락 전용 |

## 6. 커버리지 노트

- PR 리뷰 코멘트(Gemini Code Assist 피드백 포함)는 미수집 — 필요 시 `gh pr view <n> --comments`로 개별 확보 가능. "fix: AI 피드백 수정"(sehee, #58) 류 커밋으로 보아 리뷰 반영 루프가 실존 [확인].
- shlee8의 4커밋 상세 미조회 (favorite/auth 소규모).
- 2025-10~11 초기 배포 방식(Terraform 이전), 2026-03-28 이후 공백 사유는 코드로 알 수 없음 → 05-질문 목록으로.
