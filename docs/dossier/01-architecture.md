# 01. 아키텍처 맵 — Muroom Backend (bach)

> 작성일: 2026-08-02 · 기준 브랜치: `feature/studio` · 총 커밋 수: 507 (2025-10-19 최초 커밋 ~ 현재)
> 표기 규칙: **[확인]** = 코드/커밋/파일에서 직접 확인, **[추론]** = 정황상 추론 (근거 병기)

## 1. 프로젝트 개요

- **[확인]** 합주실/스튜디오 검색·등록 플랫폼 "Muroom"의 백엔드 모놀리스. 단일 Gradle 모듈(`rootProject.name = 'muroom-backend-bach'`, `settings.gradle`), 패키지 단위 모듈화 구조.
- **[확인]** 도메인: studio(+draft 임시저장), room, owner(사장님), musician(뮤지션), auth, search, map, subway, filestorage, report, inquiry, faq, terms, withdrawal, studioboasting(자랑/커뮤니티), instrument, sms, admin, common — `src/main/java/kr/muroom/muroombackendbach/` 하위 디렉토리로 확인.
- **[추론]** "bach"는 백엔드 리포 코드네임, 프론트엔드는 "handel" (CORS 허용 목록의 `muroom-frontend-handel-web.vercel.app`에서 확인, `application.yml:23`). 작곡가 이름 시리즈로 보임. → 질문 목록 행.

## 2. 기술 스택 [확인 — build.gradle]

| 영역 | 선택 | 근거 |
|------|------|------|
| 프레임워크 | Spring Boot 3.5.8, Java 21 (Corretto) | `build.gradle:3,13`, `Dockerfile` |
| DB | PostgreSQL 17 + PostGIS 3.5, Flyway 마이그레이션 | `docker-compose.yml`, `build.gradle:96-97` |
| 공간 데이터 | hibernate-spatial, jackson-datatype-jts, proj4j (좌표계 변환) | `build.gradle:70-72` |
| 캐시/세션 | Valkey 8.1.5 (Redis 호환) + spring-data-redis, ACL 파일 사용 | `docker-compose.yml`, `valkey/users.acl` |
| 쿼리 | QueryDSL 5.1.0 (jakarta) + p6spy(SQL 로깅) | `build.gradle:29,78-81,52` |
| ID 생성 | TSID (`tsid-creator` 5.2.6) — 모든 PK가 Long TSID | `build.gradle:58`, `common/util/tsid/TsidGenerator.java` |
| 인증 | jjwt 0.12.3 + auth0 java-jwt 4.4.0 (둘 다 존재), OAuth2 Client | `build.gradle:61-64,45` — 왜 JWT 라이브러리가 2개인지 → 질문 목록 |
| 외부 API | Spring Cloud OpenFeign (2025.0.0 BOM) | `build.gradle:34,89` |
| AWS | SDK v2 (S3, Secrets Manager), aws-advanced-jdbc-wrapper 2.6.8 | `build.gradle:83-86,50` |
| 문서화 | springdoc-openapi 2.8.14, Swagger UI at `/docs` | `build.gradle:107`, `application.yml:44` |
| 테스트 | Testcontainers(postgresql), spring-security-test | `build.gradle:100-104` |
| 기타 | libphonenumber, spring-context-indexer(기동 최적화) | `build.gradle:67,55` |

## 3. 모듈 구조와 레이어 규칙 [확인]

- CLAUDE.md에 명문화된 4-레이어: `presentation / application(command·query 분리) / domain(entity·valueobject·repository·enums) / exception`.
- **[확인]** CQRS-lite: 쓰기 = `application/command`(CommandService), 읽기 = `application/query`(QueryService). studio·owner 모듈에서 디렉토리 구조로 확인.
- **[확인]** 크로스 도메인 참조는 Long PK 필드만, `@OneToMany` 금지, DB FK 제약 없음 — 규칙은 CLAUDE.md, FK 제거는 마이그레이션 `V26012502__drop_studio_foreign_keys.sql`로 실증 (초기엔 FK가 있었고 나중에 제거됨 → 히스토리 단계에서 상세 추적).
- **[확인]** Entity↔DTO 변환은 도메인당 1개 Assembler (`musician/presentation/assembler`, `owner/presentation/assembler` 존재).
- **[확인]** admin 모듈은 도메인별 하위 패키지(admin/faq, admin/studio, admin/report, admin/owner, admin/withdrawal, admin/inquiry, admin/subway)로 별도 분리 — 사용자용 모듈과 관리자용 presentation을 분리하는 구조.

## 4. 공통 인프라 (common 모듈) [확인]

- **응답 규격**: `ApiResponse<T>` record — `{status, message, data}`, 실패 시 data에 `ErrorPayload(code, validationErrors)` (`common/presentation/response/ApiResponse.java`).
- **예외 처리**: `GlobalExceptionHandler` — `BusinessException`(ErrorCode 보유) / `ExternalApiException`(외부 API 전용, error 레벨 로깅 + "슬랙 연동 TODO" 주석) / AccessDenied 계열은 SecurityContext를 검사해 로그인 여부에 따라 401·403 분기 (`GlobalExceptionHandler.java:136-156`).
- **엔티티 상속**: `CreatedDateEntity → AuditableEntity → SoftDeletableEntity(@SQLDelete+@SQLRestriction)` (`common/domain/`).
- **TSID**: Hibernate `IdentifierGenerator` 커스텀 구현 (`common/util/tsid/TsidGenerator.java`) — 분산 환경에서 시간순 정렬 가능한 64-bit ID. Response DTO에서는 String으로 직렬화 (JS number 정밀도 문제 회피가 목적 **[추론]** → 질문 목록).
- **워밍업**: `WarmupListener` — 기동 시 DB 카운트 쿼리 1회 후 명시적으로 `ReadinessState.ACCEPTING_TRAFFIC` 발행 (`common/listener/WarmupListener.java`). **[추론]** ECS/ALB 헬스체크와 연계된 콜드스타트 대책. `application.yml:5-6`의 `load-on-startup: 1`, `spring-context-indexer`도 같은 목적 계열.
- **익명 사용자 컨텍스트**: `AnonymousUserContext` + `AnonymousIdSigner` — 비로그인 사용자 식별 장치 존재. 상세는 인증 딥다이브에서.

## 5. 데이터 모델 (ERD 수준) [확인 — 상세: raw/data-model.md]

- **34개 @Entity**, 전부 Long TSID PK (DB 시퀀스는 `V25122403`에서 전부 드롭 — 애플리케이션 사이드 ID 생성으로 전환).
- **JPA 임베더블 없음**: "value object"는 전부 plain record이며, 유일한 구조적 VO 저장은 `StudioDraft.studioDraftData`의 **JSONB** (`@JdbcTypeCode(SqlTypes.JSON)`).
- **경계 규칙이 실제로 관찰됨**: studio↔room/boast/subway 엣지는 Long id-ref, 나머지(musician↔instrument, term 등)는 JPA 연관. FK는 초기 스키마엔 있었고 `V26012502`에서 studio 관련만 드롭됨. 예외적으로 `SubwayStation.stationLines`는 `@OneToMany(cascade=ALL)` — "OneToMany 금지" 규칙의 예외 사례.
- 공유 PK(`@MapsId`) 3개: `StudioPrice`, `StudioBuildingInfo`, `TermContent` (1:1 확장 테이블 패턴).
- **소프트 딜리트 3종 공존**: ① `deleted_at IS NULL`(대다수) ② `is_active=true`(코드성 마스터: Term, FaqCategory, InquiryCategory, WithdrawalReason) ③ `deleted_at`+`status='INACTIVE'` 플립(Owner, Musician). 유니크 제약은 soft-delete 정합 **partial unique index**(`WHERE deleted_at IS NULL`)로 해결 — 재가입 시 닉네임/전화번호 재사용 허용 설계 [확인: V25122101, V26010901].
- 폴리모픽 신고 타겟: `Report.targetType(enum 13종) + targetId(Long)` + **jsonb `snapshot`** (신고 시점 증거 보존).
- **[확인·중요 갭]** `studios.location`/`subway_stations.location`은 `GEOGRAPHY(POINT,4326)`인데 **GiST 인덱스가 어느 마이그레이션에도 없음**. `CREATE EXTENSION postgis`도 마이그레이션에 없음(prod EC2 user_data가 수행). → 질문 목록.
- 기타 불일치: `beta_*` 테이블 3개는 SQL에만 존재(랜딩페이지 레거시 추정), 엔티티 선언 인덱스/유니크 다수가 마이그레이션에 미반영(`ddl-auto: validate`라 미생성 — 예: StudioBoastLike 중복 방지 유니크 없음), Report/Inquiry의 `deletedAt` 재선언.

## 6. 인증/인가 아키텍처 [확인 — 상세: raw/auth-subsystem.md]

- **핵심**: 런타임 인증은 **세션(JSESSIONID) 기반**. JWT는 가입/휴대폰 인증용 단기 핸드셰이크 토큰에만 실사용. `JwtAuthenticationFilter`는 시큐리티 체인에 미등록(서블릿 필터로만 자동 등록 — 체인 뒤 순서), `RefreshTokenService`(Valkey 로테이션·재사용 검출까지 구현됨)는 **호출자 0의 데드코드** [grep 재검증]. Swagger 문서에는 JWT 발급 흐름이 잔존 → **JWT→세션 전환의 고고학적 흔적**, 히스토리 단계 최우선 추적 대상.
- **뮤지션**: OAuth(Kakao/Google) — spring-oauth2-client 미사용, `RestTemplate` 수동 code 교환 → id_token의 sub 추출(**Kakao는 서명 미검증, Google은 JWKS 완전 검증** — 비대칭) → `social_accounts` 조회 → 미가입이면 signupToken(JWT) 발급 후 `/musicians/register`에서 signupToken+smsVerifyToken으로 가입 → 재로그인으로 세션 획득.
- **사장님**: 이메일+비밀번호(BCrypt), 전용 `AuthenticationManager`(DaoAuthenticationProvider). NCP SENS SMS 인증(레이트리밋 5중: TTL 3분·쿨다운 10초·폰 5회/일·IP 30회/일·실패 5회) 성공 시 PHONE_VERIFY JWT → 가입/전화변경에서 소비.
- **익명 사용자 장치**: HMAC 서명 쿠키(`anonymous_user_id`+sig, 1년) → ThreadLocal `AnonymousUserContext` → `@CurrentSubjectId`가 `"U:{id}"`/`"G:{uuid}"` 발급. 로그인 시 **게스트 즐겨찾기 → 회원 마이그레이션을 Redis Lua로** 수행.
- 인가는 `@EnableMethodSecurity` + `@PreAuthorize("hasRole('OWNER'|'MUSICIAN')")`.
- **[확인·중대]** admin 컨트롤러 7개 전부 `@PreAuthorize` 없음 + `/api/admin/**` URL 규칙 없음 → **로그인한 아무 사용자나 admin API 호출 가능**. `UserType.ADMIN`은 발급 경로 자체가 없음. WAF/게이트웨이도 없어 앞단 방어 부재 [인프라 조사와 교차 확인]. → 질문 목록 최상위.
- 기타: permitAll 경로와 실제 컨트롤러 경로 불일치 다수(`/api/v1/faq/**` vs `/faqs`, owner 가입 경로 미개방 등), CSRF 비활성+쿠키 세션 조합, 세션이 in-memory Tomcat(spring-session 없음 — desired_count=1이라 현재는 무증상).

## 7. 지리공간 검색 서브시스템 [확인 — 상세: raw/search-subsystem.md]

- **정정**: `search` 모듈은 검색 히스토리(최근 7개 + 로그) 전용. 실제 뷰포트 검색은 **studio 모듈**에 있음.
- 엔드포인트 2개(같은 `MapSearchRequest`): `GET /api/v1/studios/map-search`(마커용, 비페이징·**무제한**) / `GET /api/v1/studios/map-list`(리스트용, 페이징+정렬 — 메인).
- **쿼리 기술**: 순수 QueryDSL. PostGIS는 `Expressions.booleanTemplate("st_intersects({0}, st_makeenvelope(...4326))")`로 주입 (`StudioRepositoryImpl.java:194-205`). 거리 계산은 SQL이 아니라 **Java Haversine**. `ST_DWithin`/`ST_Distance`(geography, 미터)는 subway 모듈의 "주소 반경 2km 역 찾기"에서만 사용.
- **동적 필터 정확히 13개** (`studioFilteringWhereClause`, `:116-138`): ① 키워드(스튜디오명 LIKE OR 인근 역명 EXISTS) ② 뷰포트(필수) ③ 가격 범위(room 우선, studio_prices 폴백 OR 결합) ④ 룸 크기(mm) ⑤⑥ 공용/개별 옵션(having count = n의 **AND 시맨틱**) ⑦ 층 유형 ⑧⑨ 화장실 위치/성별(클라이언트의 단일 `restroomTypes` Set을 `@JsonCreator`가 2개 enum으로 분배) ⑩ 주차 ⑪ 숙박 ⑫ 화재보험 ⑬ 금지 악기(NOT IN). null 필터는 no-op.
- 페이징: **ID→엔티티 2단계 쿼리** + 별도 countDistinct + 인메모리 순서 복원. 정렬 화이트리스트 2개(latest, price — SQL에서 `min(basePrice) coalesce studioPrice.minPrice`).
- "가장 가까운 역" 표시는 **사장님이 등록 시 지정한 sequence** 기준이지 계산 거리가 아님; 거리 숫자만 읽기 시점 Haversine.
- **성능 관찰 [확인]**: 공간 GiST 인덱스 부재(§5), 필터 서브쿼리 조인 키 인덱스 부재, `%kw%` 선행 와일드카드(pg_trgm 없음), map-list의 즐겨찾기 행별 Redis 호출 N+1(map-search는 벌크 — 비일관), 썸네일 presign 행별 호출, `map-search` 결과 무제한, building_info에 대한 EXISTS 최대 7회 중복.

## 8. 스튜디오 도메인 & 파일 스토리지 [확인 — 상세: raw/studio-filestorage.md]

- **스튜디오 생성은 admin 전용**(`/api/admin/studios`, owner를 전화번호로 지정) — 사장님 셀프 등록 API는 아직 없음. 생성 트랜잭션 안에서: owner 조회 → **지오코딩(juso.go.kr Feign 2연쇄, 외부 HTTP in tx)** → 이미지 `PUBLIC_TEMP→PERMANENT` S3 move(검증보다 먼저) → Studio+Price+BuildingInfo(@MapsId)+Rooms+Options+ForbiddenInstruments+Images+NearbyStations 저장.
- **Draft(임시저장)**: 8단계 위저드 전체를 **단일 JSONB + TTL 3일**로 저장(`step`·`studio_name`만 컬럼 승격). 소유권은 `findByIdAndOwnerId`. 이미지는 `PRIVATE_DRAFT`(`draft/` prefix) presigned PUT. 수정 시 키 diff로 고아 이미지 softDelete.
- **[확인·버그성 3건]** ① 만료 스케줄러(`@Scheduled` 03:00 KST)가 있으나 **`@EnableScheduling`이 소스 전체에 없음** → draft 만료 정리와 withdrawal 하드삭제 스케줄러 둘 다 미실행(정리는 S3 라이프사이클 7일에만 의존, DB 행은 잔존) ② **draft→정식 스튜디오 승격 경로 미구현**(PRIVATE_DRAFT 키를 PERMANENT로 옮기는 코드 없음) ③ draft create/update 바디에 `@Valid` 누락 → 선언된 제약 비활성.
- **filestorage 정책 엔진**: `FileStorageLocation` enum이 (bucket, prefix) 정책 테이블 — PUBLIC/PRIVATE × PERMANENT/TEMP/TRASH/DRAFT/REPORT. `FileStorageService`가 유일한 공개 API(presigned PUT/GET, move=copy+hardDelete, softDelete=trash로 move, 신고 스냅샷 복사), `S3Executor`만 SDK 인지. "경로 문자열 하드코딩 금지" 규칙이 리포 전체에서 실제로 지켜짐. 앱 레벨 정리 + Terraform S3 라이프사이클(7일)의 **이중 방어**. 승격은 항상 호출측 서비스 메서드에서 명시적(이벤트/아웃박스 없음) — S3-DB 원자성 없음.
- **studioboasting**: 뮤지션 UGC(스튜디오 자랑 + 이벤트) — 이미지·좋아요(likeCount 비정규화)·중첩 댓글·신고. 스튜디오명/주소를 비정규화 저장, optional studioId 링크.

## 9. 인프라 / 배포 (AWS + Terraform) [확인 — 상세: raw/infra-deployment.md]

- **철저한 비용 최적화 설계** (서울 리전, 전부 ARM Graviton `t4g.*`):
  - **NAT 게이트웨이 대신 NAT 인스턴스**(`t4g.nano`, nftables masquerade, ~$3/mo vs ~$32/mo)
  - **RDS 대신 EC2 자체 운영 Postgres 17+PostGIS**(prod `t4g.small`, 인스턴스 위에서 이미지 빌드), **ElastiCache 대신 EC2 Valkey**
  - prod/dev가 **VPC 1개·ALB 1개 공유**(호스트 헤더 라우팅, default 404), VPC 엔드포인트는 무료 S3 Gateway만
  - Secrets Manager 외부 API 키 12개를 env당 1개 시크릿에 번들(주석에 "비용 절감" 명시)
- **트레이드오프 [확인]**: 데이터 계층 전체+NAT+dev가 **AZ 2a 단일 배치** → prod ECS의 2a+2b 스프레드는 사실상 장식, 2a 장애 = 전면 장애. NAT 단일 장애점(알람 2개가 그 방증).
- ECS on EC2(awsvpc, target_type=ip), prod 태스크 cpu1920/mem3072, `distinctInstance`+100%/200% 롤링+circuit breaker 롤백, dev는 0%/100%(다운타임 허용). 헬스체크 `/actuator/health/readiness` — `WarmupListener`의 명시적 readiness 발행과 맞물림.
- **DB 자격증명 자동 로테이션**: SAR RDS 로테이션 Lambda를 자체 운영 PG에 적용(prod 7일/dev 1일), 앱은 **aws-advanced-jdbc-wrapper의 `awsSecretsManager` 플러그인**으로 런타임 재조회 → 재배포 없이 로테이션 생존. 백업 3중(DLM EBS 스냅샷 + pg_dump→S3 일간 + **WAL 10초 주기 S3 쉬핑**).
- **배포: CI/CD 없음 [확인]** — 로컬 build/push(ECR, dev-`<sha>`/prod-v`YY.MM.DD.<letter>` 태그) → `ecs_task.tf` image 라인 수동 수정 → `deploy: <tag>` 1라인 커밋(git 히스토리로 실증) → `terraform apply`. 운영 접근은 SSM Session Manager 전용(SSH 키/배스천 없음).
- 로컬 개발: `docker-compose.yml`(inline PostGIS 빌드 + Valkey ACL — prod user_data와 동일 패턴), 앱은 `bootRun`. 앱 이미지는 멀티스테이지 Corretto 21 → 21-alpine, 비루트 실행.
- 모니터링 최소(SNS 이메일 + 알람 4개: NAT·prod PG의 status/CPU만). Terraform 상태는 S3+DynamoDB 락(별도 부트스트랩 모듈).

## 10. 대표 요청 흐름 트레이스 (코드 수준)

### ① 뷰포트 검색 — `GET /api/v1/studios/map-list`
```
StudioController.searchStudiosForMapList (studio/presentation/StudioController.java:59-70)
  → @CurrentSubjectId 해석("U:"/"G:") → SubjectParser.parse (StudioService.java:154-157)
  → 검색어 있으면 SearchHistoryService.addSearchKeyword (SearchLog 항상 + RecentSearch 회원만, 최대 7개)
  → resolveOptions: "ALL" 옵션코드 확장 (StudioService.java:282-324)
  → StudioRepositoryImpl.findStudiosForMapList (StudioRepositoryImpl.java:61-114)
      (1) ID 쿼리: st_intersects(location, st_makeenvelope(...)) + 12개 동적 predicate
          + leftJoin(room, studioPrice, buildingInfo) + groupBy + 정렬 + offset/limit
      (2) 엔티티 쿼리: id IN (...) + 인메모리 순서 복원
      (3) countDistinct 쿼리
  → 보강 조립 (StudioService.java:172-279): 룸 가격 IntSummaryStatistics 벌크 → StudioPrice 폴백,
      최소 sequence 인근역 + 노선(fetch join), 썸네일 presigned URL(행별), isFavorite Redis(행별 — N+1)
  ← PaginatedData<StudioListElementResponse> (ID는 String)
```

### ② 스튜디오 생성 — `POST /api/admin/studios` (tx 경계 = 서비스 메서드)
```
AdminStudioController.createStudio → StudioCommandService.createStudio (:79)
  → OwnerService.findByPhoneNumberOrThrowException (:80)          [크로스 모듈]
  → MapGeocodingService.getPointFromAddress (:82)                 [외부 API 2연쇄 + proj4j 변환, tx 내부]
  → fileStorageService.move(TEMP→PERMANENT) × 전 이미지 (:524-586) [S3 부수효과, 검증보다 선행]
  → 썸네일/블루프린트 검증 (:86-96)
  → Studio → StudioPrice → StudioBuildingInfo → Rooms → Options
    → ForbiddenInstruments → Images → NearbyStations 순차 저장 (:98-146)
  ← Long studioId (커밋 시점에 전체 원자적, 단 S3는 제외)
```

### ③ OAuth 로그인 — `POST /api/v1/auth/musician/login`
```
MusicianAuthController (:34-47, Origin 헤더 = redirect 베이스)
  → OAuthLoginService.login (:44): provider→클라이언트 선택
  → code 교환 (Kakao: kauth.kakao.com / Google: oauth2.googleapis.com, RestTemplate 수동)
  → id_token sub 추출 (Kakao decode-only / Google JWKS 완전 검증)
  → SocialAccountRepository.findByProviderAndProviderUserId
      hit  → SessionAuthService.login: 게스트 즐겨찾기 Lua 마이그레이션 → ROLE_MUSICIAN
             → HttpSession에 SecurityContext 저장 → JSESSIONID
      miss → signupToken(JWT) 발급, SIGNUP_REQUIRED 응답
             (이후 /musicians/register에서 signupToken+smsVerifyToken 소비 → 재로그인)
```

---

## 부록: 1단계에서 드러난 "스토리 씨앗" (2~5단계 연결 고리)

1. **JWT → 세션 전환** (데드코드 + 문서 잔재가 증거) — 의사결정 로그 후보
2. **DB FK 전면 제거** (`V26012502` + CLAUDE.md 규칙화) — 의사결정 로그 후보
3. **비용 최적화 인프라** (NAT 인스턴스, 자체 운영 DB, 시크릿 번들링 — 주석으로 의도 실증) — 케이스 스터디 후보
4. **RDS용 로테이션 Lambda를 자체 운영 PG에 적용** — 흥미로운 엔지니어링, 검증 여부 질문
5. **공간 인덱스 부재** — 성능 스토리 or 미완 과제
6. **admin 인가 부재 / 스케줄러 미활성 / draft 승격 미구현** — 진행 중인 작업의 경계선(현재 브랜치가 feature/studio)
7. **Kakao Directions 도입 후 비용 문제로 비활성화** (주석 실증) — 의사결정 로그 후보

