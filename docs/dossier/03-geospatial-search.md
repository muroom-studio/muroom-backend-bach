# 03. 딥다이브: PostGIS 뷰포트 검색과 13개 동적 필터

> 작성일: 2026-08-02 · 근거: `docs/dossier/raw/search-subsystem.md`(코드 조사) + git 고고학(develop 브랜치) + PR #5/#40 본문
> 표기: **[확인]** = 코드/커밋 근거 병기, **[추론]** = 정황 추론. 이 영역의 커밋은 별도 표기 없는 한 전부 monte-kim 저자.

---

## 1. 요약

지도 뷰포트(바운딩 박스) 안의 합주실 스튜디오를 검색하는 서브시스템. QueryDSL JPA 위에 PostGIS `st_intersects`를
템플릿 표현식으로 주입하고, 13개의 동적 필터를 `BooleanBuilder`로 조립하며, 페이징은 ID 선조회 → 엔티티 재조회 2단계로 처리한다.
흥미로운 지점: (1) 공간 쿼리를 네이티브 SQL 없이 순수 QueryDSL로 유지한 선택, (2) "도보 시간"을 Kakao Directions로 구현했다가
비용 문제로 핫픽스 제거하고 Java Haversine 직선거리로 대체한 이력, (3) 화장실 필터가 3단 진화(단일 enum → 2개 파라미터 →
단일 파라미터 + `@JsonCreator` 분배)를 거친 API 설계 흔적이 커밋 단위로 남아 있다.

---

## 2. 현재 설계

### 2.1 엔드포인트 2개 (동일 요청 DTO `MapSearchRequest` 공유)

| | A. 마커 검색 | B. 리스트 검색 ("메인") |
|---|---|---|
| URL | `GET /api/v1/studios/map-search` | `GET /api/v1/studios/map-list` |
| 진입점 | `StudioController.java:39-47` | `StudioController.java:59-70` |
| 페이징 | 없음 (**무제한**) | `@PageableDefault(sort="latest", DESC)` |
| 응답 | `StudioMapResponse` (좌표+가격범위+즐겨찾기) | `PaginatedData<StudioListElementResponse>` |
| 부수효과 | 없음 | 검색 히스토리 기록 (`StudioService.java:159-161`) |

**[확인]** 주의: `search` 패키지에는 지리공간 검색이 없다. 검색 히스토리(최근 검색어 7개 + 로그)만 담당하며,
실제 뷰포트 검색은 `studio` 모듈(`StudioController` → `StudioService` → `StudioRepositoryImpl`)에 있다.

### 2.2 쿼리 기술 — QueryDSL 템플릿으로 PostGIS 주입

네이티브 SQL/JPQL 문자열 없이 순수 QueryDSL JPA(`JPAQueryFactory` + `JPAExpressions` 서브쿼리)만 사용.
PostGIS 함수는 `Expressions.booleanTemplate`로 주입한다 [확인 — `StudioRepositoryImpl.java:194-205`].
`studio.location`은 `GEOGRAPHY(POINT, 4326)` [확인 — `Studio.java:55`, `V25120701__init.sql:170`].
`st_makeenvelope`은 geometry를 생성하므로 geography와의 비교에서 암묵 캐스트가 발생한다.

### 2.3 2단계 ID → 엔티티 쿼리 (`StudioRepositoryImpl.java:61-114`)

1. **ID 쿼리**: `select studio.id` + `leftJoin(room/studioPrice/studioBuildingInfo)` + 13개 동적 predicate
   + `groupBy(studio.id, studioPrice.minPrice)` + 정렬 + offset/limit — 조인 팬아웃으로 인한 페이징 붕괴를 groupBy로 방지.
2. **엔티티 쿼리**: `selectFrom(studio).where(studio.id.in(ids))` 후 ID 순서 보존을 위한 인메모리 재정렬 (`:92-96`).
3. **카운트 쿼리**: `countDistinct()` + 동일 조인/where (`:99-108`).

### 2.4 13개 동적 필터 (`studioFilteringWhereClause`, `StudioRepositoryImpl.java:116-138`)

입력이 없으면 각 메서드가 `null`을 반환 → `BooleanBuilder.and(null)` no-op으로 스킵되는 관용구.

| # | 요청 필드 | 생성되는 predicate |
|---|---|---|
| 1 | `keyword` | 스튜디오명 LIKE OR 인근 지하철역명 LIKE (EXISTS) |
| 2 | `min/maxLatitude·Longitude` (`@NotNull`) | `st_intersects(location, st_makeenvelope(...))` — 항상 적용 |
| 3 | `minPrice`/`maxPrice` | `EXISTS(room 가격 범위)` OR (`NOT EXISTS(가격 있는 room)` AND `EXISTS(studio_prices 범위 겹침)`) |
| 4 | `min/maxRoomWidth·Height` | `EXISTS(room, 한 룸이 width·height 둘 다 범위 내)` |
| 5 | `commonOptionCodes` | `IN (… group by having count = n)` — **AND 시맨틱(모두 보유)** |
| 6 | `individualOptionCodes` | 동일, `category='INDIVIDUAL'` |
| 7 | `floorTypes` | `EXISTS(studio_building_info.floor_type in …)` |
| 8 | `restroomTypes`→`restroomLocations` | `EXISTS(… restroom_location in …)` |
| 9 | `restroomTypes`→`restroomGenders` | `EXISTS(… restroom_gender in …)` |
| 10 | `isParkingAvailable` | true: `parking_fee_type in ('FREE','PAID')` / false: `='NONE'` |
| 11 | `isLodgingAvailable` | `EXISTS(… is_lodging_available = ?)` |
| 12 | `hasFireInsurance` | `EXISTS(… has_fire_insurance = ?)` |
| 13 | `forbiddenInstrumentCodes` | `NOT IN (…)` — 해당 악기를 금지하는 스튜디오 제외 |

전처리 2건: `restroomTypes`는 `@JsonCreator` compact 생성자가 location/gender 2개 필드로 분배(§3.3),
옵션 코드의 `"ALL"`은 쿼리 전 `StudioService.resolveOptions`(`:282-324`)가 해당 카테고리 전체 코드로 확장.

### 2.5 정렬/페이징

정렬은 화이트리스트 2개뿐 [확인 — `studioOrderSpecifiers`, `StudioRepositoryImpl.java:140-172`]:
`latest` → `studio.createdAt`, `price` → `room.basePrice.min().coalesce(studioPrice.minPrice)` + `NullsLast`.
그 외 속성은 무시(`default: break`)하고, 비어 있으면 `createdAt DESC` 폴백 — Pageable sort 주입 공격 방어를 겸한다.
거리는 SQL에서 절대 계산하지 않고 표시용으로만 Java Haversine(`MapGeocodingService.java:39-57`)을 쓴다.

---

## 3. 코드 수준 동작

### 3.1 map-list 요청 흐름 (압축 트레이스)

```
GET /api/v1/studios/map-list?minLatitude=…&…&page&size&sort
 → StudioController.searchStudiosForMapList()        StudioController.java:59-70
 → StudioService.searchStudiosForMapList()           StudioService.java:148-280
     ├ SubjectParser.parse(subjectId)                :154-157  ("U" prefix → musicianId)
     ├ searchHistoryService.addSearchKeyword(...)    :159-161  (SearchLog + RecentSearch upsert)
     ├ resolveOptions(request)                       :163      ("ALL" → 카테고리 전체 코드 확장)
     └ StudioRepositoryImpl.findStudiosForMapList()  StudioRepositoryImpl.java:61-114
         (1) ID 쿼리  (2) 엔티티 쿼리 + 재정렬  (3) countDistinct
 ← 응답 조립 StudioService.java:172-279
     룸 가격 통계 벌크 → StudioPrice 폴백 / 인근 역(최소 sequence) / presigned URL / 즐겨찾기(행마다 Redis)
```

### 3.2 핵심 코드 ① — 뷰포트 predicate (`StudioRepositoryImpl.java:194-205`)

```java
private BooleanExpression isWithinBounds(Double minLatitude, Double maxLatitude,
    Double minLongitude, Double maxLongitude) {

  return Expressions.booleanTemplate(
      "st_intersects({0}, st_makeenvelope({1}, {2}, {3}, {4}, 4326))",
      studio.location,
      Expressions.constant(minLongitude),
      Expressions.constant(minLatitude),
      Expressions.constant(maxLongitude),
      Expressions.constant(maxLatitude)
  );
}
```

### 3.3 핵심 코드 ② — 옵션 "모두 보유" AND 시맨틱 (`StudioRepositoryImpl.java:259-271`)

```java
private BooleanExpression hasAllOptionsInCategory(Set<String> optionCodes,
    OptionCategory optionCategory) {
  if (CollectionUtils.isEmpty(optionCodes)) {
    return null;
  }
  return studio.id.in(JPAExpressions.select(studioOption.studio.id)
      .from(studioOption)
      .where(studioOption.option.category.eq(optionCategory)
          .and(studioOption.option.code.in(optionCodes)))
      .groupBy(studioOption.studio.id)
      .having(studioOption.option.code.count().eq((long) optionCodes.size()))
  );
}
```

`IN + GROUP BY + HAVING count = n` — 관계 나눗셈(relational division)의 고전적 구현.
단, (studio_id, option_id) 유니크 제약이 DB에 없어 중복 행이 있으면 count가 부풀 수 있다 [확인 — raw §미해결 6].

### 3.4 핵심 코드 ③ — restroomTypes `@JsonCreator` 분배 (`MapSearchRequest.java:127-152`)

클라이언트는 `restroomTypes=["INTERNAL","SEPARATE"]` 하나만 보내면, 생성자에서 위치/성별 2개의 내부 필터로 분배된다.

```java
Set<RestroomLocation> derivedLocations = new HashSet<>();
Set<RestroomGender> derivedGenders = new HashSet<>();
if (!CollectionUtils.isEmpty(restroomTypes)) {
  for (String restroomType : restroomTypes) {
    ...
    String upperType = restroomType.toUpperCase(java.util.Locale.ROOT);
    try {
      derivedLocations.add(RestroomLocation.valueOf(upperType));
      continue;
    } catch (IllegalArgumentException ignored) {
      // Not a valid restroom location, ignore.
    }
    try {
      derivedGenders.add(RestroomGender.valueOf(upperType));
    } catch (IllegalArgumentException ignored) {
      // Invalid restroom type, ignore.
    }
  }
}
this.restroomLocations = derivedLocations.isEmpty() ? null : derivedLocations;
this.restroomGenders = derivedGenders.isEmpty() ? null : derivedGenders;
```

record 컴포넌트는 21개지만 실제 predicate는 13개 — `restroomLocations`/`restroomGenders`는 Swagger에 `hidden=true`로
숨겨진 파생 필드다. 모르는 값은 조용히 무시된다(관대한 파싱).

---

## 4. 진화 과정 (git 고고학)

### 4.1 뷰포트 조회 탄생 — 2025-11-25, PR #5 (머지 aba2504)

- **[확인]** `d80dddc` (11-25) "지도 리스트업 조회 추가": `st_intersects + st_makeenvelope` 템플릿이 **첫 커밋부터 현재와 동일한 형태**로
  등장. map-search/map-list 두 엔드포인트, subway 모듈(서울 공공데이터 동기화), Kakao Directions 클라이언트, JTS 설정까지 한 커밋에 포함.
  초판 map-list는 필터 없이 bounds + `leftJoin(studioPrice)` + offset/limit뿐이었고, 파일 안에
  `//  private OrderSpecifier<?> getOrderSpecifier()` 주석 스텁이 남아 있었다.
- **[확인]** `3a4f986` (같은 날 2시간 뒤) "정렬 추가": 스텁이 구현되면서 **2단계 ID→엔티티 쿼리 구조도 이날 도입** —
  현재 코드의 주석 "정렬 및 페이징을 적용하여 스튜디오 ID 목록을 먼저 조회"가 이 커밋에서 처음 등장 (`git log -S`로 확인).
- **[확인]** PR #5 본문에 지하철 동기화의 자동 vs 수동 고민이 기록됨 (§5.2에서 상술).

### 4.2 필터의 시초는 PR #14가 아니라 PR #11 — 2025-11-30

- **[확인]** `c47abb2` (11-30, PR #11) "스튜디오 조회 필터링 추가": 첫 필터 세트 등장. 이때는 **단일 `optionCodes`**,
  화장실은 **단일 enum `RestroomType`**(INTERNAL/EXTERNAL/SHARED/PRIVATE — 위치와 성별 개념이 한 enum에 혼재),
  가격/룸 크기/층/주차/숙박/화재보험/금지악기 포함. `Room` 엔티티와 `StudioForbiddenInstrument`도 이 커밋에서 태어났다.

### 4.3 필터 완성 + 키워드 — 2025-12-01, PR #14 (머지 9bfd50e)

- **[확인]** `a4a2884` (12-01): `optionCodes` → `commonOptionCodes` + `individualOptionCodes` 분리 + `"ALL"` 확장 처리.
- **[확인]** `cd44589` (12-01) "스튜디오 검색 기능 추가": 키워드 검색 도입. **초판은 역 ID를 별도 쿼리로 선조회한 뒤 IN 절**에
  넣는 2-쿼리 방식이었고, 당시 `subwayStationNearbyStudio.studio.id`처럼 **엔티티 참조**를 썼다.
- **[확인]** `8345291` (12-01, "PR#14 코드 리뷰 반영"): `JPAExpressions.selectOne()...exists()` 패턴이 이 커밋에서 등장
  (`git log -S "JPAExpressions.selectOne"`) — 선조회 IN 방식이 상관 EXISTS 서브쿼리로 리팩토링됨. [추론] Gemini Code Assist
  리뷰 반영 커밋이므로 AI 리뷰 피드백이 계기였을 가능성이 높다.
- **[확인]** `9936646` (12-03) "스튜디오 인근 역 조회": `ST_DWithin`/`ST_Distance`가 **subway 모듈에** 도입.
  studio 모듈 히스토리에는 `ST_DWithin` 흔적이 전혀 없다(`git log -S` 빈 결과) — 스튜디오 검색에 반경 쿼리를 쓰려던 시도는 없었고,
  처음부터 뷰포트(envelope) 방식으로 일관했다.

### 4.4 화장실 필터 2차 진화 — 2025-12-06

- **[확인]** `5e3da37` (12-06) "null 허용 적용 및 스튜디오 옵션 변경": 단일 `RestroomType` enum을 폐기하고
  `RestroomLocation` + `RestroomGender` 두 개의 **독립 요청 파라미터**로 분리. 도메인 모델 정합성은 올랐지만 클라이언트가
  2개 파라미터를 다뤄야 하는 API가 됨.

### 4.5 도보시간 → Haversine 핫픽스 — 2025-12-14, PR #40 (hotfix/distance, 머지 008acbb)

- **[확인]** 삭제 전 구현 (`git show d80dddc:...MapDirectionService.java`): `@Async CompletableFuture<Integer>
  getWalkingTimeMinutes(start, end)` — Kakao Mobility Directions API(`apis-navi.kakaomobility.com/v1/directions`)를 호출해
  duration을 분으로 올림. map-list 조립부는 **검색 결과의 스튜디오마다** future를 만들고 `CompletableFuture.allOf(...).join()`으로
  대기했다 [확인 — 4dec468 diff의 삭제 라인]. 즉 검색 1페이지당 최대 N회의 유료 외부 API 호출이 발생하는 구조.
- **[확인]** `4dec468` (12-14): `@Service`를 주석 처리하고 `@deprecated 비용 문제로 더 이상 도보 시간을 사용하지 않으므로
  이 서비스는 제거됐습니다` Javadoc을 남김. 같은 커밋에서 `MapGeocodingService.calculateDistanceInMeters`(Haversine, R=6371km,
  정수 미터 반올림)를 추가하고 응답 필드를 `walkingTimeMinutes` → 직선거리로 교체.
- **[확인]** PR #40 본문의 "기타 질문: 없음" — 비용이라는 사유는 PR 본문이 아니라 **코드의 @deprecated 주석에만** 기록되어 있다.
  브랜치명이 `hotfix/distance`인 점에서 운영 중 비용 인지 후 긴급 대응이었음을 시사 [추론].
- **[정정 2026-08-03]** 전날 기록한 "공공데이터 보행 API 중간 시도" 증언은 재확인 결과 **기억 혼선** — 해당 공공데이터 API는
  도로명주소 검색 API(juso.go.kr)로 **지오코딩 용도**이며 도보시간과 무관 [확인: 코드·전체 이력 pickaxe]. 도보시간 서사는
  "Kakao 비용 → Haversine" 2단으로 확정. "10초당 5회 제한" 기억의 귀속은 재확인 중 — 유력 가설: 벌크 지오코딩
  (`findNearbyStationsInBulk`, 호출자 0 데드코드)이 Juso rate limit로 폐기된 건. 확정 시 04-decisions B2-b에 반영.

### 4.6 화장실 필터 3차 진화 + restroom 분배 — 2025-12-16, PR #45 (머지 c6293ce)

- **[확인]** `510e9db` (12-16) "restroom type 기반 필터링 추가": 4.4의 2-파라미터 API를 다시 **단일 `restroomTypes`
  파라미터로 통합**하되, 내부적으로는 `@JsonCreator` compact 생성자가 location/gender로 분배(§3.4). 기존 2개 필드는
  `hidden=true`로 유지. 같은 커밋에서 모든 컬렉션 파라미터를 `List` → `Set`으로 일괄 전환.
- 결과적으로 화장실 필터는 "단일 enum(11-30) → 도메인 분리 2-파라미터(12-06) → 클라이언트 편의 단일 파라미터 + 서버측
  분배(12-16)"의 3단 진화 — 도메인 순수성과 API 사용성 사이의 왕복 기록.

### 4.7 JPA 가이드라인 정리 — 2026-01-25

- **[확인]** `fb8fbbb` (01-25) "studio jpa 가이드라인 적용": PR #88 묶음(FK 전면 드롭 `V26012502`와 동시기). matchKeyword가
  현재의 `subwayStationNearbyStudio.studioId`(plain Long) 형태로 정착 — 타 도메인은 PK만 참조한다는 현행 CLAUDE.md 규칙이
  이 시점에 코드에 반영됨. 이후 검색 코어는 6개월 이상 무변경 (마지막 수정 2026-01-25).

---

## 5. 대안의 흔적

### 5.1 도보 시간 — Kakao(비용) → 공공데이터(쿼터) → 자체 계산

- 도보 시간은 초기 설계(11-25)부터 포함된 1급 기능이었다(전용 서비스 + `@Async` 병렬화 + Feign 인터셉터 인증까지 구축).
  19일 만에 제거 [확인 — §4.5]. 다만 `KakaoDirectionsApiClient` 빈과 API 키 설정은 현재도 잔존
  [확인 — `KakaoDirectionsApiClient.java:9-21`, raw §미해결 11] — 재도입 여지를 남긴 것인지 정리 누락인지는 불명 [추론].
- [정정 2026-08-03] "공공데이터 보행 API 시도" 증언은 기억 혼선으로 철회 — §4.5 정정 참조. 공공데이터(Juso)는
  지오코딩 용도이며, "10초당 5회" rate limit 기억의 귀속(벌크 지오코딩 폐기 가설)은 Q6에서 확인 중.

### 5.2 지하철 동기화 — 자동 vs 수동

- **[확인]** PR #5 본문 원문: "지하철역과 노선 정보를 가져오는 API를 추가했는데, 이 API를 서버 시작할 때 자동으로 1회
  수행하도록 하는 게 좋을까? 아니면 관리자 권한으로 서버 시작 여부와 상관없이 호출하는 게 좋을까?"
- 최종 채택: 수동 admin 엔드포인트 `POST /api/admin/subway/sync` (`AdminSubwayStationController.java:17-21`), 스케줄러 없음
  [확인]. 지하철역 마스터 데이터는 변경 빈도가 낮아 수동 운영이 합리적이라는 판단 [추론].

### 5.3 기타 발견한 흔적

- **키워드 검색 선조회 IN → EXISTS**: §4.3 — 코드 리뷰 반영 커밋(8345291)에서 교체된, 남아 있지 않은 대안.
- **반경(ST_DWithin) 검색의 부재**: 스튜디오 검색에 반경 쿼리를 시도한 커밋이 전무 [확인 — `git log -S "ST_DWithin"` 빈 결과].
  반경 검색은 지도 UI와 맞지 않아 처음부터 envelope 방식을 선택한 것으로 보인다 [추론].
- **"가장 가까운 역" = 사장님이 정한 순서**: 검색 결과의 대표 역은 거리 계산이 아니라 등록 시 지정한 최소 `sequence`
  [확인 — `StudioService.java:190-197`]. 자동 최근접 계산 대신 운영자 큐레이션을 택한 형태.
- **SubwayService의 벌크 조회 주석**: `SubwayService.java:75-76`에 "이 부분은 DB 조회를 반복하지만 … 극단적인 최적화가
  필요하다면 하나의 쿼리로 합칠 수 있으나"라는 트레이드오프 주석이 남아 있고, 해당 메서드 `findNearbyStationsInBulk`는
  현재 src/main에 호출자가 없다 [확인 — 데드코드].

---

## 6. 성능/제약 (현상 → 영향 → 개선안)

| 현상 | 영향 | 개선안 한 줄 |
|---|---|---|
| `studios.location`에 GiST 인덱스가 마이그레이션에 없음 [확인 — 전체 grep, `ddl-auto: validate`] | `st_intersects`가 시퀀셜 스캔 → 데이터 증가 시 선형 열화 | `CREATE INDEX ... USING GIST(location)` 마이그레이션 1건 |
| 필터 서브쿼리 조인 키(rooms.studio_id 등)에도 인덱스 없음 (FK는 V26012502에서 드롭, Postgres는 참조측 자동 인덱스 없음) | EXISTS/IN 서브쿼리마다 풀 스캔 | 조인 키 4곳에 btree 인덱스 |
| N+1 4건: ① map-list 즐겨찾기 행마다 Redis SISMEMBER (`StudioService.java:275`) ② 썸네일 키마다 presigned URL ③ 등록/수정 시 역 `findById` 루프 ④ `findNearbyStationsInBulk` 주소별 공간 쿼리 | 페이지 크기에 비례한 왕복 증가 | ①은 이미 존재하는 벌크 `getFavoriteStudioIds`로 교체(map-search는 이미 사용 중) |
| 키워드 `%kw%` 선행 와일드카드 [확인 — `containsIgnoreCase`] | btree 사용 불가, 항상 스캔 | `pg_trgm` + GIN 인덱스 |
| map-search(A) limit 전무 | 넓은 뷰포트 요청 시 전체 매칭 행 반환 → 메모리/응답 폭주 | 상한(예: 500) + 줌 레벨별 클러스터링 |
| 13개 필터 중 7개가 같은 `studio_building_info`에 대한 개별 EXISTS | 후보 행당 동일 1:1 테이블 최대 6회 접근 | building_info 조건을 단일 EXISTS로 병합 |
| min-price 폴백 로직이 SQL(정렬)과 Java(표시)에 독립 구현 2벌 | 정렬 순서와 표시 가격 불일치 가능 | 폴백 규칙을 한쪽(SQL 표현식)으로 단일화 |

---

## 7. 면접 예상 질문 씨앗

1. **"왜 네이티브 쿼리 대신 QueryDSL 템플릿으로 PostGIS를 호출했나?"** — 13개 동적 필터와 공간 조건을 하나의 타입 세이프
   빌더에서 조합하기 위해. 공간 함수만 `booleanTemplate`로 국소화해 나머지는 컴파일 타임 검증을 유지.
2. **"ID 선조회 2단계 쿼리는 왜 필요한가?"** — room 조인 팬아웃 상태에서 offset/limit을 걸면 페이지가 깨짐. groupBy로
   중복을 접은 ID만 페이징하고 엔티티는 IN으로 재조회, 순서는 인메모리 재정렬로 보존. (첫날부터 이 구조였다는 점도 언급 가능.)
3. **"도보 시간을 왜 없앴고, 대안 검토는?"** — 검색 1페이지당 N회의 유료 Directions 호출 구조라 비용이 트래픽에 비례.
   무료 공공데이터 보행 API로 대체를 시도했으나 10초당 5회 쿼터로 해당 유스케이스에 부적합 [증언] → 외부 의존 자체를
   제거하고 Haversine 자체 계산으로 회귀. "유료는 비용, 무료는 쿼터"라는 이중 제약 하의 실용적 후퇴 서사. (캐싱/사전계산
   대안을 검토했는지는 본인 답변으로 보강 필요.) 클라이언트 빈은 잔존(재도입 여지).
4. **"옵션 '모두 보유' 필터는 어떻게 구현했나?"** — GROUP BY + HAVING count = 선택 개수(관계 나눗셈). 전제는
   (studio, option) 중복 없음 — DB 유니크 제약이 없어서 이 부분이 약점임을 스스로 지적하면 가점.
5. **"GiST 인덱스 없이 st_intersects가 지금 잘 도는 이유는?"** — 현재 스튜디오 행 수가 작아 시퀀셜 스캔도 충분히 빠름.
   성장 시 병목 1순위이며, geography 컬럼 + GiST에서 `st_intersects(geography, geometry)` 암묵 캐스트가 인덱스를 태우는지
   실행계획 검증까지 언급하면 깊이를 보여줄 수 있다.

---

## 부록: 검색 코어 커밋 연표 (전부 monte-kim, 머지 커밋만 계정 `monte`)

| 날짜 | 커밋 | 내용 |
|---|---|---|
| 2025-11-25 | d80dddc | 뷰포트 검색 탄생 (st_intersects 템플릿, 2 엔드포인트, subway/Kakao 포함) — PR #5 |
| 2025-11-25 | 3a4f986 | 정렬 + 2단계 ID→엔티티 쿼리 도입 |
| 2025-11-30 | c47abb2 | 첫 필터 세트 (단일 optionCodes, 단일 RestroomType) — PR #11 |
| 2025-12-01 | a4a2884 | common/individual 옵션 분리 + "ALL" 확장 — PR #14 |
| 2025-12-01 | cd44589 | 키워드 검색(스튜디오명 OR 역명) — PR #14 |
| 2025-12-01 | 8345291 | 리뷰 반영: 선조회 IN → EXISTS 상관 서브쿼리 |
| 2025-12-03 | 9936646 | subway에 ST_DWithin 인근 역 조회 |
| 2025-12-06 | 5e3da37 | RestroomType → Location/Gender 분리 |
| 2025-12-14 | 4dec468 | 도보시간 제거 → Haversine (hotfix/distance) — PR #40 (머지 008acbb) |
| 2025-12-16 | 510e9db | restroomTypes 단일 파라미터 + @JsonCreator 분배, List→Set — PR #45 |
| 2026-01-25 | fb8fbbb | JPA 가이드라인 적용 (엔티티 참조 → Long studioId) — 이후 무변경 |
