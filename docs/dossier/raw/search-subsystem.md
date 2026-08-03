# [RAW] 지리공간 검색 서브시스템 조사 결과 (에이전트 원본, 2026-08-02)

> 03-딥다이브의 원천 자료. 모든 내용은 코드에서 확인된 사실이며, 파일:라인 근거 포함.

핵심 정정: `search` 패키지에는 지리공간 검색이 **없다**. 검색 히스토리(최근 검색어 + 로그)만 있다. 뷰포트 검색은 **`studio` 모듈** (`StudioController` → `StudioService` → `StudioRepositoryImpl`)에 있으며 `room`, `subway`, `map`이 뒷받침한다.

---

## 1. 검색 엔드포인트

### 1a. `search` 모듈 (히스토리 전용)

| 파일 | 내용 |
|---|---|
| `search/presentation/SearchController.java:18-36` | `GET /api/v1/search-histories/recent`, `@PreAuthorize("hasRole('MUSICIAN')")`, 최근 키워드 ≤7개 반환 |
| `search/application/SearchHistoryService.java:24` | `MAX_RECENT_SEARCH_COUNT = 7` |
| `search/application/SearchHistoryService.java:30-44` | `addSearchKeyword(musicianId, keyword)` — `SearchLog` 기록(항상) + `RecentSearch` upsert(회원만) |
| `search/application/SearchHistoryService.java:65-86` | `findByMusicianAndKeyword`로 중복 제거; 7개 초과분 오래된 것 삭제 |
| `search/domain/entity/SearchLog.java:45-57` | `byMusician` / `byAnonymousUser` (익명 id는 `AnonymousUserContext`에서) |
| `search/domain/repository/RecentSearchRepository.java:11-13` | Spring Data 파생 쿼리만 — QueryDSL/공간 쿼리 없음 |

map-list 검색에서 호출됨: `studio/application/StudioService.java:159-161`.

### 1b. 실제 지리공간 검색 — 전체 요청 흐름

같은 요청 DTO를 쓰는 bounds 엔드포인트가 **2개**.

**엔드포인트 A — 핀/마커 검색 (비페이징)**

```
GET /api/v1/studios/map-search
  → StudioController.searchStudiosInMapBounds()      studio/presentation/StudioController.java:39-47
  → StudioService.searchStudiosInMapBounds()          studio/application/StudioService.java:66-114
      ├ resolveOptions(request)                       StudioService.java:282-324   ("ALL" 옵션코드 확장)
      └ StudioRepository.findStudiosWithinBounds()    studio/domain/repository/StudioRepository.java:7
          → StudioRepositoryImpl.findStudiosWithinBounds()   StudioRepositoryImpl.java:50-58
              queryFactory.selectFrom(studio).distinct()
                .where(studio.deletedAt.isNull(), studioFilteringWhereClause(request))
                .fetch()
  ← 응답 조립: StudioService.java:76-113 (룸 가격 통계 벌크, StudioPrice 폴백, Redis 즐겨찾기)
  ← DTO: StudioMapResponse (id, name, lat, lng, minPrice, maxPrice, isFavorite)
```

**엔드포인트 B — 리스트 검색 (페이징+정렬) — "메인" 검색**

```
GET /api/v1/studios/map-list?minLatitude&maxLatitude&minLongitude&maxLongitude&…&page&size&sort
  → StudioController.searchStudiosForMapList()        StudioController.java:59-70
       @PageableDefault(sort = "latest", direction = DESC)   :65
  → StudioService.searchStudiosForMapList()           StudioService.java:148-280
       ├ SubjectParser.parse(subjectId) → "U" prefix일 때만 musicianId   :154-157
       ├ searchHistoryService.addSearchKeyword(...)   :159-161
       ├ resolveOptions(request)                      :163
       └ StudioRepository.findStudiosForMapList(req, pageable)
           → StudioRepositoryImpl.findStudiosForMapList()    StudioRepositoryImpl.java:61-114
               (1) ID 쿼리  : select studio.id
                               left join room          on room.studioId = studio.id           :66
                               left join studioPrice   on studioPrice.id = studio.id          :67
                               left join studioBuildingInfo on ...id = studio.id              :68
                               where deleted_at is null AND <13개 동적 predicate>              :70-73
                               group by studio.id, studioPrice.minPrice                       :74
                               order by studioOrderSpecifiers(pageable)                       :75
                               offset/limit                                                    :76-77
               (2) 엔티티 쿼리: selectFrom(studio).where(studio.id.in(ids))                    :86-89
                   + ID 순서 보존을 위한 인메모리 재정렬                                        :92-96
               (3) 카운트 쿼리: countDistinct() + 동일 조인/where                              :99-108
  ← 응답 조립 StudioService.java:172-279
  ← DTO: PaginatedData<StudioListElementResponse>
```

**쿼리 기술:** 순수 **QueryDSL JPA** (`JPAQueryFactory`, `JPAExpressions` 서브쿼리). 네이티브 SQL/JPQL 문자열 없음. PostGIS 함수는 QueryDSL **템플릿 표현식**(`Expressions.booleanTemplate`/`numberTemplate`)으로 주입.

**PostGIS 호출 (검색 경로의 유일한 공간 predicate):** `StudioRepositoryImpl.java:194-205`

```java
Expressions.booleanTemplate(
    "st_intersects({0}, st_makeenvelope({1}, {2}, {3}, {4}, 4326))",
    studio.location, minLng, minLat, maxLng, maxLat);
```

`ST_Intersects` + `ST_MakeEnvelope` — `ST_Within`/`ST_Contains` 아님. `studio.location`은 `GEOGRAPHY(POINT, 4326)` (`Studio.java:55`, `V25120701__init.sql:170`). geography/geometry 혼용: `st_makeenvelope`은 *geometry*를 생성하므로 암묵적 캐스트 발생.

거리는 검색 경로 SQL에서 **절대 계산 안 함** — Java에서 Haversine (`map/application/MapGeocodingService.java:39-57`). `ST_Distance`/`ST_DWithin`은 subway 모듈에서만.

**키워드 검색:** `StudioRepositoryImpl.matchKeyword()` `:174-192`. trigram/tsvector 없음 — QueryDSL `containsIgnoreCase` = `lower(studio.name) like lower('%kw%')` OR `EXISTS(subway_stations_nearby_studios ⋈ subway_stations, 역명 containsIgnoreCase)`.

**페이징:** ID→엔티티 2단계 쿼리 + 별도 `countDistinct()`, `PageImpl` 래핑. `map-search`(A)는 비페이징·무제한.

**정렬:** `studioOrderSpecifiers()` `:140-172` — 화이트리스트 2개:
- `latest` → `studio.createdAt` (`:150`)
- `price` → `room.basePrice.min().coalesce(studioPrice.minPrice)` + `NullsLast` (`:153-158`)
- 그 외 속성 무시(`default: break`, `:160`); 비어있으면 `createdAt DESC` 폴백 (`:167-169`)

### 1c. 동적 필터 — 정확히 13개

`studioFilteringWhereClause()` (`StudioRepositoryImpl.java:116-138`)에서 조립. 입력이 없으면 각자 `null` 반환 → `BooleanBuilder.and(null)` no-op.

| # | 요청 필드 (`MapSearchRequest.java`) | 빌더 라인 | 생성되는 predicate |
|---|---|---|---|
| 1 | `keyword` (:18) | `:119` → `matchKeyword` `:174-192` | 스튜디오명 LIKE OR 인근 지하철역명 LIKE (EXISTS) |
| 2 | `min/maxLatitude`, `min/maxLongitude` (:22-34, `@NotNull`) | `:120-122` → `isWithinBounds` `:194-205` | `st_intersects(location, st_makeenvelope(...))` — 항상 적용 |
| 3 | `minPrice`/`maxPrice` (:45,:48) | `:123` → `matchPriceRange` `:207-243` | `EXISTS(room base_price between …)` OR (`NOT EXISTS(room with price)` AND `EXISTS(studio_prices 범위 겹침)`) |
| 4 | `min/maxRoomWidth`, `min/maxRoomHeight` (:51-60) | `:124-125` → `hasMatchingRoomSize` `:245-257` | `EXISTS(room, width_mm·height_mm 둘 다 범위 내)` — 한 룸이 둘 다 만족 |
| 5 | `commonOptionCodes` (:38) | `:126` → `hasAllOptionsInCategory(COMMON)` `:259-271` | `IN (select studio_id … group by having count = n)` — **AND 시맨틱(모두 보유)** |
| 6 | `individualOptionCodes` (:42) | `:127-128` | 동일, `category='INDIVIDUAL'` |
| 7 | `floorTypes` (:63) | `:129` → `inFloorTypes` `:273-283` | `EXISTS(studio_building_info floor_type in …)` |
| 8 | `restroomTypes`→`restroomLocations` (:69) | `:130` → `inRestroomLocations` `:297-307` | `EXISTS(… restroom_location in …)` |
| 9 | `restroomTypes`→`restroomGenders` (:72) | `:131` → `inRestroomGenders` `:309-319` | `EXISTS(… restroom_gender in …)` |
| 10 | `isParkingAvailable` (:75) | `:132` `:321-338` | true: `parking_fee_type in ('FREE','PAID')` / false: `='NONE'` |
| 11 | `isLodgingAvailable` (:78) | `:133` `:285-295` | `EXISTS(… is_lodging_available = ?)` |
| 12 | `hasFireInsurance` (:81) | `:134` `:340-350` | `EXISTS(… has_fire_insurance = ?)` |
| 13 | `forbiddenInstrumentCodes` (:84) | `:135` → `notForbidsInstruments` `:352-360` | `NOT IN (select studio_id from studio_forbidden_instruments …)` — 해당 악기 금지 스튜디오 제외 |

전처리 특기사항:
- `restroomTypes`는 클라이언트향 단일 `Set<String>`을 `@JsonCreator` compact 생성자(`MapSearchRequest.java:87-157`)가 `RestroomLocation.valueOf` → `RestroomGender.valueOf` 시도로 분배 (모르는 값 무시, `:139-147`). 선언된 record 컴포넌트 21개 → predicate 13개.
- `commonOptionCodes`/`individualOptionCodes`의 `"ALL"`은 쿼리 전 해당 카테고리 전체 코드로 확장 (`StudioService.resolveOptions`, `:282-299`) — 요청당 `optionRepository.findAllByCategory` 왕복 추가.

헬퍼 `between(NumberPath, min, max)` `:365-374` — `between`/`goe`/`loe`/`null`.

---

## 2. `map` 모듈 — 지오코딩

**외부 API (둘 다 Feign):**

| 클라이언트 | 파일 | Base URL | 오퍼레이션 |
|---|---|---|---|
| `JusoApiClient` | `map/infrastructure/client/JusoApiClient.java:9-32` | `https://business.juso.go.kr/addrlink` | `addrLinkApi.do`(주소 검색), `addrCoordApi.do`(주소→좌표) |
| `KakaoDirectionsApiClient` | `map/infrastructure/client/KakaoDirectionsApiClient.java:9-21` | `https://apis-navi.kakaomobility.com` | `GET /v1/directions` |

- Kakao 인증 헤더는 Feign `RequestInterceptor` (`map/config/KakaoFeignClientConfig.java:17-22`).
- **Kakao Directions는 비활성**: `MapDirectionService`는 `@Deprecated`, `@Service` 주석 처리 (`map/application/MapDirectionService.java:11-18`, 비용 문제로 제거했다는 주석). 살아있는 지오코딩은 **Juso(juso.go.kr)뿐**.

**좌표 변환 (proj4j):** `map/application/CoordinateTransformService.java`
- `EPSG:5179`(GRS80/UTM-K) → `EPSG:4326`(WGS84), `:15-19`
- `transformGRS80toWGS84(x,y)` `:21-43` — 호출마다 `CRSFactory`/`BasicCoordinateTransform` 새로 생성(캐시 없음)

**`MapGeocodingService`:**
- `getPointFromAddress` `:59-106` — ① 주소검색(1건) ② 좌표조회(admCd/rnMgtSn/udrtYn/buldMnnm/buldSlno) ③ proj4j 변환 + BigDecimal 6자리 반올림 → JTS Point. 실패 시 `ExternalApiException`
- `getPointsFromAddresses` `:114-126` — `parallelStream()`(공용 ForkJoinPool) → ConcurrentMap
- `calculateDistanceInMeters` `:39-57` — Haversine, R=6371.0km, 정수 미터
- `GeometryFactory` 빈: `map/config/JtsConfig.java:21-24`, SRID 4326

**`getPointFromAddress` 호출처:** 스튜디오 생성(`StudioCommandService.java:82`), 수정(`:166`), 주차위치 지오코딩(`StudioQueryService.java:138`), 지하철 인근 조회(`SubwayService.java:31`)

---

## 3. `subway` 모듈 — 인근 역 계산

**외부 API:** 서울 열린데이터 — `SeoulSubwayClient` (`subway/infrastructure/client/SeoulSubwayClient.java:8-13`), `http://openapi.seoul.go.kr:8088`, `subwayStationMaster`.

**적재:** `SubwayDataBatchService` — `MAX_FETCH_SIZE=1000`(`:30`), totalCount 프로브 후 전체 페이지 순회(`:64-74`), 봉투 코드 `INFO-000` 검증(`:89-97`), 이름 키 맵으로 upsert(`:101-142`). 트리거는 수동: `POST /api/admin/subway/sync` (`admin/subway/AdminSubwayStationController.java:17-21`). 스케줄러 없음.

**저장:** `SubwayStation.location` = `geography(Point,4326) NOT NULL`; `SubwayStationNearbyStudio` — `sequence` + `@ManyToOne subwayStation` + **plain `Long studioId`**, soft-delete.

**공간 쿼리 (`ST_DWithin`/`ST_Distance` 유일 사용처):** `SubwayStationRepositoryImpl.java:20-48` — geography라 단위는 **미터**. 반경 `SEARCH_RADIUS_METERS = 2000` (`SubwayService.java:27`). 거리 오름차순.

**호출 경로:**
- `GET /api/v1/subway/nearby?address=…` — 스튜디오 **등록 시** 사장님이 역을 고르는 용도. 검색 시에는 호출 안 됨.
- `findNearbyStationsInBulk` `:64-102` — 주소별 루프 DB 쿼리(주석으로 인정, `:75-76`). **src/main에 호출자 없음.**
- 선택된 역 저장: `StudioCommandService.buildNearbyStations`(`:588-608`, `createStudio`의 `:144-146`에서 호출), `updateNearbyStations`(`:400-434`), 삭제 `:452`. 스트림 안에서 역마다 `findById`(`:594`, `:415`).
- 검색 결과에서 역 읽기: `findAllByStudioIdInWithStation`(fetch join, `SubwayStationNearbyStudioRepositoryImpl.java:18-25`) 후 스튜디오별 **최소 `sequence`** 선택(`StudioService.java:190-197`) — "가장 가까운 역" = 사장님이 정한 순서이지 계산된 거리가 아님. 표시 거리는 Java Haversine(`StudioService.java:245-246`).

---

## 4. `room` 모듈 — 검색 결과의 가격

- `Room` — `width_mm`/`height_mm`, `isAvailable`, `availableAt`, `basePrice`(nullable), `Long studioId`(연관관계 없음), soft-delete.
- `RoomRepository` — `findAllByStudioIdIn`, `findAllByStudioId`, `deleteAllByStudioId`. 집계 쿼리 없음.

**검색이 가격 집계를 반환하나? — min/max를 Java에서 계산.**
- `map-search`: `StudioService.java:80-86` 룸 벌크 로드 → `Collectors.summarizingInt(Room::getBasePrice)` → `calculatePriceWithPrefetched`(`:476-501`)가 min/max 취하고 **룸에 가격이 없으면 `StudioPrice.minPrice/maxPrice` 폴백**(`:489-495`).
- `map-list`: 동일 패턴 인라인 `:175-181`, `:222-235`.
- 레거시 단건 경로 `calculatePrice`(`:116-146`).
- **정렬은 SQL에서** `min(room.basePrice) coalesce studioPrice.minPrice` — 표시 가격은 Java에서 — 같은 폴백 규칙의 독립 구현 2개 (불일치 가능).

---

## 성능 관련 세부사항

**인덱스**
- `studios.location`/`subway_stations.location`에 대한 **GiST 인덱스가 마이그레이션에 전혀 없음** (전체 grep 검증). `ddl-auto: validate`이므로 Hibernate도 생성 안 함. → 시퀀셜 스캔 (out-of-band 생성 여부는 질문).
- 필터 서브쿼리 조인 키(`subway_stations_nearby_studios.studio_id`, `rooms.studio_id`, `studio_options.studio_id`, `studio_forbidden_instruments.studio_id`)에도 인덱스 없음. FK는 `V25120701`에 있었으나 `V26012502`가 studio FK를 드롭; Postgres는 FK 참조측 자동 인덱스 없음.
- 키워드 검색 `%kw%` 선행 와일드카드 — btree 불가, `pg_trgm`/GIN 없음.

**쿼리 셰이프 / N+1 (모두 확인됨)**
- `map-list`: 페이지 자체 3 SQL + 보강 벌크 ~5 + `resolveOptions`(0–2) + 검색 히스토리 쓰기.
- **N+1 ①** `map-list`의 즐겨찾기: `isFavoriteStudio(studio.getId(), subjectId)`를 행마다 호출(`StudioService.java:275`) → Redis `SISMEMBER` × N. 벌크 버전 `getFavoriteStudioIds`(`StudioFavoriteQueryService.java:25-39`)는 이미 존재하고 `map-search`는 사용(`:93`).
- **N+1 ②** presigned URL: 썸네일 키마다 `fileStorageService.getViewUrl` (`:217-219`).
- **N+1 ③** 스튜디오 생성/수정의 역 `findById` 루프(`StudioCommandService.java:594`, `:415`).
- **N+1 ④** `findNearbyStationsInBulk` 주소별 공간 쿼리 + 노선 조회(`SubwayService.java:77-99`).
- `getPointsFromAddresses` — 블로킹 Feign을 공용 ForkJoinPool `parallelStream()`으로.
- `CoordinateTransformService` — 호출마다 CRS 객체 재생성.
- `map-search`(A) — **limit 전혀 없음**: 넓은 뷰포트면 전체 매칭 스튜디오+룸 반환.
- ID 쿼리의 `leftJoin(room)` 행 팽창 → `groupBy`; 카운트도 동일 팬아웃 조인으로 스캔. room/buildingInfo 조인은 `price` 정렬에만 필요한데 무조건 존재.
- 13개 필터 중 7개가 같은 `studio_building_info` 행에 대한 개별 EXISTS — 1:1 데이터를 후보 행당 최대 6회 조회.

**페치 전략**
- `open-in-view: false` (전 프로파일). DTO 조립은 `@Transactional` 서비스 안.
- `default_batch_fetch_size` 미설정.
- 보강 읽기에는 명시적 `fetchJoin()` 사용 (`SubwayStationNearbyStudioRepositoryImpl.java:22,31,41`, `SubwayStationLineRepositoryImpl.java:22,31,40`).
- 다이얼렉트는 plain `PostgreSQLDialect` + hibernate-spatial.
- `@SQLRestriction("deleted_at IS NULL")`이 암묵 predicate 추가; `findStudiosWithinBounds`는 명시적 `deletedAt.isNull()`도 추가(중복).

---

## 미해결 질문 (에이전트 제기)

1. 공간 GiST 인덱스가 Flyway 밖(수동 DDL)에서 생성됐는가?
2. `st_intersects(geography, geometry)` 캐스트 방향과 인덱스 사용 여부?
3. 뷰포트 min>max/경도 180° 교차에 대한 검증 없음 — 의도?
4. `map-search` 무제한 — 클라이언트가 뷰포트 크기로 제한하는 전제?
5. min-price 로직 이중 구현(SQL 정렬 vs Java 표시) 불일치 가능 — 의도?
6. `hasAllOptionsInCategory`의 count= 로직은 (studio_id, option_id) 유니크 전제 — DB 유니크 제약 미발견.
7. `restroomTypes` 미인식 값 무시 — 의도된 API 계약?
8. `findNearbyStationsInBulk` 호출자 없음 — 데드코드?
9. "가장 가까운 역" = owner-assigned sequence — 의도된 제품 동작?
10. 지하철 sync 주기 — 수동 운영?
11. Kakao Directions 비활성인데 클라이언트 빈/키 요구는 잔존 — 유지 의도?
