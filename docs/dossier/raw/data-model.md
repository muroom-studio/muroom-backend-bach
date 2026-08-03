# [RAW] 데이터 모델 / ERD 조사 결과 (에이전트 원본, 2026-08-02)

> `$J` = `src/main/java/kr/muroom/muroombackendbach/`. 총 **34개 @Entity**, 전부 Long PK.
> `@Embeddable`/`@Embedded`/`@ElementCollection`/`@Converter` 없음 (grep 검증) — "value object"는 JPA 임베더블이 아니라 **jsonb로 직렬화되는 plain record**.

## 0. 공통 베이스 & TSID

- `CreatedDateEntity` — `@MappedSuperclass`, `createdAt OffsetDateTime` (`@CreatedDate`, updatable=false)
- `AuditableEntity extends CreatedDateEntity` — `updatedAt` (`@LastModifiedDate`)
- `SoftDeletableEntity extends AuditableEntity` — `deletedAt` + 제네릭 `@SQLRestriction("deleted_at IS NULL")`/`@SQLDelete` (서브클래스 대부분 자체 재선언)
- TSID: `@Tsid` = `@IdGeneratorType(TsidGenerator.class)`; `TsidFactory` NODE_BITS=8(256노드), UTC, ThreadLocalRandom, 싱글턴. **애플리케이션 사이드 ID 생성** — DB 시퀀스는 `V25122403__drop_all_sequences.sql`에서 전부 드롭.
- sms·filestorage 모듈엔 엔티티 없음 (Redis 스토어 + S3만).

## 1. 모듈별 엔티티 (요점)

### studio
- **Studio (`studios`)** — SoftDeletable. name(100), 주소 3필드, introduction TEXT, depositAmount, thumbnailImageKey/blueprintImageKey(1024), viewCount, `location Point` = `geography(Point,4326)`, **`ownerId Long`(크로스 도메인 id-ref)**
- **StudioBuildingInfo / StudioPrice** — `@OneToOne @MapsId`로 studio PK 공유 (FK `NO_CONSTRAINT`). BuildingInfo: FloorType/RestroomLocation/RestroomGender/ParkingFeeType enum(STRING), floorNumber, 주차/숙박/화재보험 필드. Price: minPrice/maxPrice
- **StudioImage** — ManyToOne→Studio, StudioImageCategory(MAIN, BUILDING, ROOM, BLUEPRINT, COMMON_OPTION, INDIVIDUAL_OPTION), imageKey, sequence
- **StudioOption** — Studio↔Option 조인 엔티티; **Option** — OptionCategory(COMMON/INDIVIDUAL/ETC), code(unique), iconImageKey
- **StudioForbiddenInstrument** — ManyToOne→Studio + ManyToOne→**Instrument(크로스 모듈 엔티티 참조)**
- **StudioViewLog** — 베이스 없음, `@CreationTimestamp viewedAt`, ManyToOne→Musician(nullable)/Studio, byMusician/byAnonymousUser XOR (DB CHECK)
- **StudioDraft (`studio_drafts`)** — AuditableEntity(소프트삭제 아님). `ownerId Long`, `step`(1~8), `studioName`, `expiresAt`, **`studioDraftData` = `@JdbcTypeCode(SqlTypes.JSON)` jsonb** (record `StudioDraftData`: 주소, 역, 건물, 룸, 이미지키 리스트, 옵션/악기 코드)

### room
- **Room (`rooms`)** — SoftDeletable. name, sequence, `width_mm`/`height_mm`, isAvailable, availableAt, `basePrice`(nullable), **`studioId Long`(id-ref)**. `DiscountType`/`RoomInfo`/`DiscountBenefit`는 Room에 비영속 — StudioDraft JSON 안에서만 사용.

### owner / musician
- **Owner (`owners`)** — AuditableEntity + 자체 `deletedAt`/`hardDeleteAt`. email(unique)/password/name/phone/nickname(unique)/experienceYears, UserStatus. `@SQLDelete`가 deleted_at + `status='INACTIVE'` 동시 세팅.
- **Musician (`musicians`)** — 동일 패턴 + `@OneToOne(LAZY)` → **Instrument** (NOT NULL, 크로스 모듈 참조)
- **MyStudio** — ManyToOne→Musician, 이름/주소
- UserStatus: ACTIVE, INACTIVE, BLOCKED, UNVERIFIED

### auth
- **SocialAccount (`social_accounts`)** — ManyToOne→Musician(nullable), OAuthProvider(KAKAO/NAVER/GOOGLE), providerUserId, unique(provider, provider_user_id). `UserType(OWNER/MUSICIAN/ADMIN)`은 컬럼에 매핑 안 됨.

### search
- **RecentSearch** — keyword, `@CreationTimestamp`/`@UpdateTimestamp`, ManyToOne→Musician(NOT NULL)
- **SearchLog** — CreatedDateEntity, `createdAt`→`searched_at` `@AttributeOverride`, anonymousUserId, ManyToOne→Musician(nullable, XOR)

### subway
- **SubwayStation** — 베이스 없음, name, `location geography(Point,4326) NOT NULL`, **`@OneToMany(cascade=ALL, orphanRemoval)` stationLines** (프로젝트 내 유일한 OneToMany 사용례 중 하나)
- **SubwayLine** / **SubwayStationLine**(조인) / **SubwayStationNearbyStudio** — sequence + ManyToOne→SubwayStation + **`studioId Long`(id-ref)**, soft-delete

### report
- **Report** — ManyToOne→Musician(reporter_id) + ManyToOne→ReportReason + **폴리모픽 타겟: `ReportDomainType targetType` + `targetId Long`** + `snapshot jsonb NOT NULL`. `deletedAt` 재선언(베이스와 중복).
- **ReportReason**(code unique, isActive, sequence) / **ReportReply**

### inquiry
- **Inquiry** — ManyToOne→Musician/InquiryCategory, InquiryStatus(PROCESSING/COMPLETED), `@OneToOne(mappedBy)`→InquiryReply, `@OneToMany(cascade=ALL)`→InquiryImage. `deletedAt` 재선언.
- **InquiryCategory** — **`is_active=true` 방식 소프트삭제** (`@SQLDelete`가 is_active=false)
- **InquiryReply**(OneToOne owning→Inquiry) / **InquiryReplyImage**

### faq / terms / withdrawal / instrument
- **Faq**(ManyToOne→FaqCategory) / **FaqCategory**(CreatedDateEntity, is_active 방식)
- **Term** — TermsType/TargetRole enum, version, isMandatory, is_active 방식; **TermContent** — `@MapsId` PK 공유; **MusicianAgreement/OwnerAgreement** — ManyToOne→Term+Musician/Owner, `agreedAt @CreatedDate`
- **MusicianWithdrawal** / **WithdrawalReason**(is_active 방식)
- **Instrument** — code(unique), description

### studioboasting
- **StudioBoast** — thumbnailImageFileKey, content, 스튜디오명/주소 비정규화, `agreedToEventTerms`, instagramAccount, `likeCount` 비정규화, **`creatorUserId Long` + `studioId Long`(nullable) id-ref**
- **StudioBoastImage**(ManyToOne→Boast) / **StudioBoastLike**(ManyToOne→Musician+Boast, NO_CONSTRAINT) / **StudioBoastComment**(content, `creatorUserId`/`taggedUserId` Long, **self-ref parent_id**) / **StudioBoastCommentLike**(**`musicianId Long`** — 같은 모듈의 StudioBoastLike는 엔티티 참조인데 이건 id-ref: 유일한 모듈 내 비일관)

## 2. 관계 그래프 (크로스 도메인 Long id-ref 목록)

```
Studio→Owner(owner_id), Room→Studio(studio_id), StudioDraft→Owner(owner_id),
SubwayStationNearbyStudio→Studio(studio_id), StudioBoast→Musician(creator_user_id)/Studio(nullable),
StudioBoastComment→Musician(creator/tagged), StudioBoastCommentLike→Musician,
Report→<폴리모픽>(target_id+target_type), StudioDraftData JSON 내 subwayStationId
```

관찰된 경계 규칙: **studio/room/boast/subway↔studio 엣지는 Long id-ref** (`V26012502`에서 FK 적극 드롭), musician/instrument/term 등 나머지는 JPA 연관 유지.

## 3. 공간 컬럼 & 인덱스

| 테이블 | 컬럼 | SQL 타입 |
|---|---|---|
| `studios` | `location` | `GEOGRAPHY(POINT,4326) NOT NULL` (init.sql:170) — 엔티티에선 nullable |
| `subway_stations` | `location` | `GEOGRAPHY(POINT,4326) NOT NULL` (init.sql:301) |

- **`CREATE EXTENSION postgis` 문이 어느 마이그레이션에도 없음** — DB에 사전 설치 전제 (prod EC2 user_data가 CREATE EXTENSION 수행).
- **GiST/공간 인덱스가 어느 마이그레이션에도 없음** (grep 검증). `ddl-auto: validate`라 Hibernate도 생성 안 함.
- 존재하는 인덱스: studio_drafts(owner_id, expires_at), faq_categories(code), boast comment 계열 4개+like 2개, introductory_images, 그리고 **soft-delete 정합 partial unique** — `unq_musicians_nickname_active`/`phone_number_active`(V25122101), `unq_owners_nickname/phone/email_active`(V26010901), 모두 `WHERE deleted_at IS NULL`.

## 4. 불일치 / 특기사항

**SQL에만 있고 엔티티 없음**
- `beta_registrations`, `beta_introductory_images`, `beta_inquiries` (init.sql:397-436) — Java 참조 전무. 베타/랜딩페이지 레거시. 시퀀스도 드롭 대상에서 제외됨.
- `studio_boast_images`에 DB 트리거 `update_updated_at_column()` (V25122503) — Hibernate `@LastModifiedDate`와 이중 기록.

**엔티티 선언 DDL이 실체화 안 됨** (`ddl-auto: validate`이므로 JPA 선언 인덱스/유니크는 생성되지 않음)
- `StudioBoastImage`의 @Index, `StudioBoastLike`의 unique+@Index 2개 — 마이그레이션에 없음 → **좋아요 중복이 DB 레벨에서 방지 안 됨** (반면 CommentLike의 unique는 V25122701에 있음)
- `SubwayLine.name` unique=true — SQL에선 주석 처리(init.sql:331)
- `SocialAccount` 유니크 제약 이름 상이(엔티티 `uk_provider_provider_user` vs SQL `unq_social_accounts_provider_user`)

**deletedAt 재선언 (중복/잠재 버그)**
- `Report`, `Inquiry`가 SoftDeletableEntity 상속 + `deletedAt` 재선언 (같은 컬럼 섀도잉)
- `Owner`/`Musician`은 AuditableEntity 상속 + 자체 deletedAt/hardDeleteAt — status 플립과 결합된 의도적 설계

**엔티티 vs SQL nullability 드리프트** (validate는 nullability 미검사 → 무증상)
- studios의 주소/location/이미지키: SQL NOT NULL vs 엔티티 optional
- rooms.width/height: SQL NULL vs 엔티티 nullable=false
- inquiries/faqs/reports의 deleted_at은 NOT NULL로 만들어졌다가 V25121901/V25122005/V25122502에서 완화 (소프트삭제 도입기의 시행착오 흔적)
- FaqCategory는 CreatedDateEntity(updatedAt 없음)인데 테이블엔 updated_at NOT NULL (V25122002)

**소프트 딜리트 3종 공존**
1. `deleted_at IS NULL` (대다수)
2. `is_active = true` (Term, FaqCategory, InquiryCategory, WithdrawalReason — 마스터/코드성 데이터)
3. deleted_at + `status='INACTIVE'` 플립 (Owner, Musician — 사용자 계정)

**기타**
- 공유 PK(@MapsId) 3개: StudioBuildingInfo, StudioPrice, TermContent
- `Owner.java:13` Instrument 데드 임포트
- `UserType`은 entity 패키지에 있지만 컬럼 매핑 없음
