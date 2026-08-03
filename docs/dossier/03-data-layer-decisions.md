# 03. 딥다이브: 데이터 계층 의사결정 — TSID, FK 제거, 소프트 딜리트

> 작성일: 2026-08-02 · 근거: git 고고학(46b37e2, 0e6a8ca, d887210, a741e98 등) + `docs/dossier/raw/data-model.md` + `docs/dossier/02-history.md`
> 표기: **[확인]** = 커밋/파일:라인 근거 병기, **[추론]** = 정황 추론. 저자 표기: monte-kim = 본인, 2-say = 팀원 Sehee(맥락 전용).

## 1. 요약

- **결정 ①**: DB 시퀀스(테이블당 1개, `allocationSize=1`) 기반 PK를 2025-12-24 하루에 **앱 사이드 TSID로 전환**(46b37e2)하고 시퀀스 30개를 전부 드롭. 같은 날 3시간 뒤 **모든 응답 DTO의 ID를 String으로 전환**(0e6a8ca) — JS 53bit 정밀도 문제의 즉각 대응.
- **결정 ②**: init.sql엔 FK 27개가 있었으나, 2026-01-25 `V26012502`에서 **studio/subway 관련 FK 14개를 드롭**(d887210). 이후 CLAUDE.md에 "FK 미설정" 규칙으로 성문화(a6709fa, 2026-03-08).
- **결정 ③**: 소프트 딜리트는 `deleted_at`/`is_active`/`status 플립` **3종 공존**. `deleted_at NOT NULL` 오설계 3건의 완화(팀원 작업), **partial unique index**(musicians는 본인, owners는 팀원)로 탈퇴 후 재가입 시 유니크 재사용 문제 해결.

---

## 2. 결정 ①: 시퀀스 → TSID

### 2.1 배경 — 초기 시퀀스 전략

- **[확인]** Flyway 도입 시점의 `V25120701__init.sql`(f7e2cb7, monte-kim, PR #27)은 **테이블당 전용 시퀀스 24개**를 생성(`CREATE SEQUENCE studio_id_seq;` 등, init.sql:1-24)하고, PK 컬럼에 `DEFAULT nextval(...)`을 걸었다(예: `studio_id BIGINT NOT NULL DEFAULT nextval('studio_id_seq')`, init.sql:165). `DEFAULT nextval` 사용처는 24곳.
- **[확인]** 엔티티 측은 `GenerationType.SEQUENCE` + `@SequenceGenerator(..., allocationSize = 1)` — 46b37e2 diff의 Room.java/Studio.java 삭제부에서 확인. `allocationSize=1`이므로 **INSERT마다 DB에 nextval 왕복**이 발생하는 구성이었다.
- **[확인]** 이후 팀원(2-say)의 inquiry/faq/withdrawal 마이그레이션들이 같은 패턴으로 시퀀스를 추가해, 드롭 시점엔 총 30개가 존재했다(`V25122403`의 DROP 30건).

### 2.2 전환 — 46b37e2 (2025-12-24, monte-kim)

- **[확인]** 커밋 메시지 "fix: TSID 적용 및 DDL 반영". 37개 파일 변경(+188/-253): 엔티티 33개에서 `@GeneratedValue/@SequenceGenerator`를 제거하고 `@Tsid`로 교체, `V25122403__drop_all_sequences.sql`로 시퀀스 30개를 `DROP SEQUENCE ... CASCADE`(CASCADE라서 컬럼의 `DEFAULT nextval`도 함께 제거됨).
- **[확인]** TSID 자체의 첫 도입은 이틀 전 **246e3c2(2025-12-22, monte-kim, "studio boast 등록 및 조회 추가")** — 신규 도메인(studioboasting)에서 먼저 검증하고, 12-24에 전 엔티티로 확산시킨 순서다. `common/util/tsid/Tsid.java`와 `TsidGenerator.java` 모두 246e3c2에서 생성된 뒤 수정 이력 없음 → **NODE_BITS=8은 최초 설계값 그대로**.
- **[확인]** 구현 상세(`Tsid.java`): `@IdGeneratorType(TsidGenerator.class)` 커스텀 어노테이션, f4b6a3 TsidFactory에 `NODE_BITS=8`(주석: "최대 256개 노드(서버/컨테이너) 지원"), UTC Clock, `ThreadLocalRandom` 랜덤 함수, 싱글턴 팩토리(`INSTANCE`). `TsidGenerator.generate()`가 `toLong()`으로 BIGINT에 저장.
- **[확인]** 같은 커밋에서 신규 테이블 DDL(`V25122401__create_studio_boasts.sql`)은 PK를 `BIGINT NOT NULL` **DEFAULT 없이** 선언 — 이후 모든 신규 테이블 DDL 관행이 됨.
- **[추론]** 전환 동기는 커밋에 명시돼 있지 않다. 정황상 (a) `allocationSize=1`의 INSERT당 왕복 제거, (b) 분산/다중 인스턴스 대비(NODE_BITS 주석), (c) 시간 정렬 가능한 ID — 특히 boast 같은 피드형 도메인의 커서 페이지네이션 적합성. boast 도메인에서 먼저 도입된 점이 (c)를 뒷받침한다.

### 2.3 파생 결정 — 응답 ID String화 (0e6a8ca, 같은 날 16:41)

- **[확인]** TSID 커밋(13:31)으로부터 약 3시간 뒤, 0e6a8ca "response dto의 id 타입을 Long에서 String으로 수정 **(JS에서 53bit까지만 읽어들여, id값이 누락되는 이슈 해결)**". 33개 파일 — auth/faq/inquiry/instrument/studio/studioboasting/subway/terms/user/withdrawal 전 모듈의 응답 DTO 약 20개 + 이를 조립하는 서비스 9개.
- 원인: 시퀀스 ID는 작은 정수라 문제가 없었지만, TSID는 64bit 상위 비트까지 사용하므로 JS `Number`(IEEE754 double, 정수 안전 범위 2^53-1)에서 하위 비트가 유실된다. 즉 **TSID 전환이 당일 프론트 장애성 이슈를 낳았고, 당일 String화로 봉합**한 인과 구조다. [확인 — 커밋 메시지 자체가 인과를 명시]
- **[확인]** PR #51 본문에 *"진짜 이렇게 해야 되나? 실무에서도 이렇게 해?"*라는 자문이 남아 있다(02-history.md §2 채록). 답은 "그렇다": Twitter Snowflake API의 `id_str` 병기가 동일한 이유로 존재하는 업계 표준 패턴이다.
- **[확인]** 이 규칙은 CLAUDE.md에 "Response DTO에서 ID는 String 타입으로 반환"으로 성문화됐다(a6709fa).

### 2.4 트레이드오프

| 얻은 것 | 근거 |
|---|---|
| 시간 정렬 가능한 PK (생성순 = ID순) | TSID 스펙 + UTC Clock 설정 [확인] |
| INSERT 시 DB 왕복 제거(앱 사이드 생성), 영속화 전 ID 확보 | allocationSize=1 → @Tsid 전환 diff [확인] |
| 256노드까지 무충돌 분산 생성 | NODE_BITS=8 주석 [확인] |
| UUID(128bit) 대비 절반 크기로 BIGINT/인덱스 효율 유지 | toLong() 저장 [확인] |

| 지불한 것 | 근거 |
|---|---|
| JS 53bit 문제 → 전 응답 DTO String화 비용 | 0e6a8ca [확인] |
| ID에 생성 시각이 인코딩됨 → 노출 시 생성 시점 유추 가능 | TSID 구조상 자명 [확인], 실제 위협 평가는 없음 [추론] |
| 랜덤 성분이 ThreadLocalRandom(비암호학적) → 근접 시각 ID 추측 난이도 낮음 | Tsid.java:33 [확인], 리스크 수용 여부는 미기록 [추론] |
| node id를 명시 설정하지 않아(빌더에 withNode 없음) 기본 동작(환경변수/랜덤)에 의존 — 다중 컨테이너에서 노드 충돌 가능성 관리가 암묵적 | Tsid.java 빌더 체인 [확인], 운영 리스크 여부는 [추론] |

---

## 3. 결정 ②: DB FK 전면 제거

### 3.1 초기엔 FK가 있었다

- **[확인]** `V25120701__init.sql`은 FK를 적극 사용: `fk_studios_on_owner`, `fk_rooms_on_studio`, `fk_musicians_on_instrument` 등 27개(beta 포함). 즉 "FK 없는 설계"는 **처음부터의 사상이 아니라 운영 중 전환된 결정**이다.
- **[확인]** 전조: TSID 전환과 같은 날 만들어진 boast 테이블들(V25122401, V25122503, 모두 monte-kim)은 FK 선언을 **주석 처리**한 채 생성됐다(`-- CONSTRAINT fk_studio_boasts_on_creator_user ...`). 즉 monte의 신규 테이블에서는 2025-12-24부터 이미 FK-less 관행이 시작됐고, 한 달 뒤 기존 테이블로 소급 적용된 것이다. 반면 같은 시기 팀원(2-say)의 inquiry/faq/withdrawal 테이블(V25121601~V25122002)은 FK를 계속 생성했다 — 컨벤션이 아직 팀 규칙이 아니었다는 증거.

### 3.2 V26012502 — studio 관련 FK 14개 드롭 (d887210, 2026-01-25, monte-kim, PR #88)

- **[확인]** 커밋 "feat: studio 관리자 수정/삭제 기능 추가"(+884/-348)의 일부. Studio 수정/삭제 기능(StudioCommandService 604라인 신설)을 만들면서 삭제 시 FK 제약이 걸림돌이 되는 시점에 드롭이 이뤄졌다.
- **[확인]** 드롭 목록(V26012502 전문 확인):
  - Studio 계열 10개: `fk_rooms_on_studio`, `fk_studio_images_on_studio`, `fk_studio_options_on_studio/option`, `fk_studio_forbidden_instruments_studio/instrument`, `fk_studio_prices_on_studio`, `fk_studio_building_info_on_studio`, `fk_studio_view_logs_on_studio`, `fk_studios_on_owner`
  - Subway 계열 4개: `fk_subway_stations_nearby_studios_on_studio/subway_station`, `fk_subway_station_lines_on_subway_station/subway_line`
- **[확인]** 드롭하지 않은 FK: musician↔instrument, terms 계열(term_contents, agreements), my_studios, social_accounts, recent_searches, search_logs, `fk_studio_view_logs_on_musician`, inquiry/faq/withdrawal 계열, beta 계열. 즉 **"전면 제거"는 studio 도메인 경계에 한정된 부분 제거**이며, 팀원 소유 도메인의 FK는 현존한다.
- **[추론]** 드롭 동기(커밋에 미기록): 소프트 딜리트와 FK의 충돌이 직접 원인일 가능성이 높다. 같은 커밋의 V26012501이 studio 서브엔티티 6개 테이블에 `deleted_at`을 추가했는데, 소프트 딜리트 체계에서 부모 행이 논리 삭제돼도 물리적으론 남아 FK가 하는 일이 없고, 반대로 하드 삭제·재구성 시나리오에선 FK가 순서 제약만 만든다. 관리자 수정 기능(자식 전체 교체 패턴)에서 FK 제약이 운영 마찰을 일으켰을 것이다.

### 3.3 규칙화 — CLAUDE.md (a6709fa, 2026-03-08, monte-kim, feature/studio)

- **[확인]** `git log -- CLAUDE.md` 결과 단일 커밋 a6709fa "feat: claude context 추가". 규칙 원문(당시 diff 그대로): *"DB 레벨 외래키 제약 없음 (FK 미설정) / 동일 도메인 내: `@ManyToOne`으로 엔티티 객체 참조 / `@OneToMany` 절대 금지 / 타 도메인 참조 시: 해당 엔티티의 PK 필드(Long)만 참조 / Cascade 등 연관 제약은 Application Service 레벨에서 직접 관리"*. 실행(12-24 관행 → 01-25 소급 드롭)이 먼저, 문서화(03-08)가 나중인 순서다.

### 3.4 현재 관계 그래프 (raw/data-model.md §2 재사용)

- **엔티티 참조(@ManyToOne, FK 없음/NO_CONSTRAINT)**: 동일 도메인 내부(StudioImage→Studio, Room… ) + musician/instrument/term 등 팀원 도메인.
- **Long id-ref(크로스 도메인)**: `Studio→Owner(owner_id)`, `Room→Studio(studio_id)`, `StudioDraft→Owner`, `SubwayStationNearbyStudio→Studio`, `StudioBoast→Musician/Studio(nullable)`, `StudioBoastComment→Musician(creator/tagged)`, `Report→폴리모픽(target_type+target_id)`.
- **[확인]** 모듈 내 유일한 비일관: StudioBoastLike는 Musician 엔티티 참조인데 StudioBoastCommentLike는 `musicianId Long`(raw §1 studioboasting).

### 3.5 트레이드오프

- **무결성의 애플리케이션 위임 [확인]**: 고아 자식(예: studio 삭제 후 rooms 잔존) 방지는 StudioCommandService의 절차적 코드에 의존. 소프트 딜리트가 기본이라 실제 하드 삭제가 드물어 리스크가 낮다는 게 암묵 전제 [추론].
- **인덱스 관련 — 중요한 정정**: PostgreSQL은 FK의 **참조하는 쪽(referencing) 컬럼에 인덱스를 자동 생성하지 않는다**. 실제로 init.sql엔 beta용 1개를 제외하면 보조 인덱스가 전무했다 [확인 — grep 결과 `idx_introductory_images_on_registration_id`뿐]. 따라서 **FK 드롭으로 인덱스가 사라진 것은 없다** — 애초에 없었다. `rooms.studio_id`, `studio_images.studio_id` 등은 지금도 미인덱스 상태이며 [확인], 이는 FK 결정과 무관한 별도 부채다. 대조적으로 monte의 후기 테이블은 명시적 인덱스를 동반한다(V25122701 boast comment 6개+like 2개, V26030801 studio_drafts 2개) [확인].
- **폴리모픽 참조 가능**: Report의 target_type+target_id, StudioBoast의 nullable studio_id는 FK로는 표현 불가한 모델링을 허용 [확인].

---

## 4. 결정 ③: 소프트 딜리트 3종 공존

### 4.1 3종 전략과 적용 대상 (raw §4 재사용 + 코드 근거)

| 전략 | 대상 | 이유 |
|---|---|---|
| ① `deleted_at IS NULL` | 대다수(Studio, Room, boast 계열 등) | 일반 레코드. `SoftDeletableEntity`의 제네릭 `@SQLDelete`/`@SQLRestriction`(SoftDeletableEntity.java:20-21) [확인] |
| ② `is_active = true` | Term, FaqCategory, InquiryCategory, WithdrawalReason | 코드성 마스터 데이터 — "삭제"가 아니라 "비활성화"가 의미론적으로 맞음. `@SQLDelete`가 is_active=false 세팅 [확인, 대상 선정 이유는 추론] |
| ③ `deleted_at` + `status='INACTIVE'` 플립 | Owner, Musician | 사용자 계정 — 탈퇴(deleted_at)와 계정 상태(UserStatus: ACTIVE/INACTIVE/BLOCKED/UNVERIFIED)를 함께 관리, `hard_delete_at`으로 지연 물리삭제 예약. `@SQLDelete`가 두 컬럼 동시 세팅(Owner.java:32-36, Musician.java:34-38) [확인] |

- **[확인]** `SoftDeletableEntity` 계층은 최초 베타 커밋 **1c0075f(2025-11-09, monte-kim)** 부터 존재 — CreatedDateEntity/AuditableEntity와 함께 프로젝트 첫 기능 커밋에서 도입된 본인 설계다.
- **[확인]** Owner/Musician은 SoftDeletableEntity를 **상속하지 않고** AuditableEntity + 자체 deletedAt/hardDeleteAt — status 플립과 결합하기 위한 의도적 이탈(raw §4).

### 4.2 `deleted_at NOT NULL` 완화 3건 — 시행착오 서사 (전부 팀원 Sehee 작업, 맥락 전용)

- **[확인]** 세 테이블(inquiries, faqs, reports)이 `deleted_at TIMESTAMPTZ NOT NULL`로 잘못 생성됐다가 각각 완화됐다. 저자 확인 결과 **3건 모두 2-say(Sehee)**:
  - `V25121901__update_inquiries.sql` — 3edcf3e, 2-say, 2025-12-19 ("1:1 문의 답글 생성 API 추가")
  - `V25122005__update_faq.sql` — 2d97ef7, 2-say, 2025-12-21 ("faq 조회 API 추가")
  - `V25122502__update_reports.sql` — e59e432, 2-say, 2025-12-25 ("신고하기 등록 / 삭제 추가")
- 서사: `deleted_at`은 "미삭제 = NULL"이 전제인데 NOT NULL로 만들면 INSERT 즉시 실패한다. 생성 마이그레이션과 실제 기능 구현 사이에 시차가 있어, **각 기능의 첫 쓰기 경로를 구현할 때마다 하나씩 발견·수정**된 패턴이다 [추론 — 커밋 메시지와 일자 간격이 정합]. 이는 팀원 영역의 시행착오이므로 본인 스토리로 서술하지 말 것. 단, 소프트 딜리트 **컨벤션 창시자(1c0075f)는 본인**이므로 "패턴 확산 과정에서 팀이 겪은 온보딩 비용"이라는 프레임은 가능하다.

### 4.3 Partial unique index — 재가입 시 유니크 재사용 해결

- 문제: 소프트 딜리트 하에서 일반 UNIQUE 제약은 탈퇴(논리삭제)한 계정의 nickname/phone/email이 영구 점유되어 **재가입·신규가입이 막힌다**.
- **[확인]** 해법: 활성 행에만 유니크를 거는 partial unique index.
  - **musicians**: `V25122101__update_musicians.sql` — 기존 `unq_musicians_nickname/phone_number` DROP 후 `CREATE UNIQUE INDEX ... WHERE deleted_at IS NULL`. 커밋 저자는 **monte-kim**(a741e98, 2025-12-24 13:48, "fix: flyway 버전 맞추기" — TSID 커밋 17분 뒤). pickaxe(`-S unq_musicians_nickname_active`) 결과 이 내용이 등장한 커밋은 a741e98이 유일 [확인]. 단, 커밋 메시지가 "버전 맞추기"이고 musicians(user 도메인)는 Sehee 담당 영역이라, **설계 발의가 누구였는지는 git만으로 단정 불가** — "커밋 기록상 본인 저자, 버전 충돌 정리 과정에서 반입" 수준으로만 주장할 것 [추론 주의].
  - **owners**: `V26010901__update_owners.sql` — nickname/phone/email 3종 동일 패턴. 저자 **2-say(Sehee)**, 1f25acd, 2026-01-09 ("feat: 로그인 검증 추가") [확인]. **팀원 작업 — 맥락 전용, 본인 작업으로 서술 금지.**
- **[확인]** 결과적으로 현재 partial unique 5개: `unq_musicians_nickname_active`, `unq_musicians_phone_number_active`, `unq_owners_nickname/phone_number/email_active` — 전부 `WHERE deleted_at IS NULL`(raw §3).
- 남은 구멍 [확인, raw §4]: `Report`/`Inquiry`가 SoftDeletableEntity 상속 + `deletedAt` 재선언(컬럼 섀도잉), `StudioBoastLike`의 엔티티 선언 unique가 마이그레이션에 없어 **좋아요 중복이 DB 레벨에서 안 막힘**(ddl-auto: validate라 실체화 안 됨).

---

## 5. 부수 발견

- **34개 @Entity, 전부 Long PK, @Embeddable 제로** — "value object"는 JPA 임베더블이 아니라 jsonb 직렬화 plain record(StudioDraftData 등) [확인, raw 서두].
- **엔티티-SQL 드리프트와 validate의 한계** [확인, raw §4]: `ddl-auto: validate`는 nullability와 인덱스/유니크를 검사하지 않는다 → studios 주소·location NOT NULL(SQL) vs optional(엔티티), rooms.width/height는 반대 방향, SubwayLine.name unique는 SQL에서 주석 처리, SocialAccount 유니크 제약 이름 불일치 — 모두 무증상 잠복.
- **beta_* 잔존 테이블 3개** [확인]: `beta_registrations/introductory_images/inquiries`(init.sql:397-436) — Java 참조 전무, 시퀀스도 V25122403 드롭 대상에서 제외됨(beta 시퀀스 3개는 현존). 베타 랜딩(PR #1)의 화석.
- **@MapsId 공유 PK 3개** [확인]: StudioBuildingInfo, StudioPrice, TermContent — 1:1 확장 테이블을 부모 PK 공유로 구현(FK는 NO_CONSTRAINT).
- **`studio_boast_images`의 DB 트리거** `update_updated_at_column()`(V25122503) — Hibernate `@LastModifiedDate`와 이중 기록 [확인].
- **공간 인덱스 부재** [확인, raw §3]: geography 컬럼 2개(studios, subway_stations)에 GiST 인덱스가 어떤 마이그레이션에도 없음.

## 6. 면접 예상 질문 씨앗

1. **"왜 UUID(v4/v7)가 아니라 TSID였나?"** — 답변 포인트: 64bit로 BIGINT 호환(기존 스키마 무변경, 인덱스 크기 절반), 시간 정렬성은 UUIDv7과 동급, 문자열 UUID 대비 저장/조인 비용. 반론 수용: UUIDv7은 표준이고 128bit라 추측 난이도가 높다 — 서비스 규모에선 TSID의 실용성 우위 판단. NODE_BITS=8(256노드) 선택 근거까지 말하면 가점.
2. **"TSID로 바꾸자마자 프론트에서 ID가 깨졌다. 왜?"** — JS Number의 2^53-1 정수 안전 범위. 당일 발견→당일 전 DTO String화(0e6a8ca), Twitter `id_str` 선례. "직렬화 계층에서 Long→String 일괄 처리(예: Jackson ToStringSerializer)로 못 했나?"라는 후속 질문 대비 — 실제론 DTO 타입을 일일이 바꿨고, 공통 Serializer가 더 나은 대안이었을 수 있다고 자기 평가 [추론].
3. **"FK 없이 참조 무결성은 어떻게 보장하나?"** — 소프트 딜리트가 기본이라 물리 삭제 자체가 드묾 + 삭제/교체 오케스트레이션을 CommandService에 집중(StudioCommandService) + 크로스 도메인은 Long id-ref로 결합도 자체를 낮춤. 정직한 인정: 고아 검출 배치나 정합성 모니터링은 없음. "그럼 FK를 남겨두면 안 됐나?"에는 소프트 딜리트 하에서 FK의 실효가 낮고, 관리자 수정의 자식 전체 교체 패턴에서 순서 제약만 남더라는 실경험으로 응수.
4. **"소프트 딜리트와 UNIQUE 제약이 충돌하면?"** — 탈퇴자가 닉네임/전화번호를 영구 점유하는 문제 → `CREATE UNIQUE INDEX ... WHERE deleted_at IS NULL`(partial unique index). 심화: 이 방식은 PostgreSQL 전용이며, MySQL이라면 deleted_at을 유니크 키에 포함시키는 우회(sentinel 값)가 필요함을 비교 설명.
5. **"소프트 딜리트 전략이 3개인 건 설계 부채 아닌가?"** — 대상의 성격이 다르다는 방어: 코드성 마스터는 '비활성화'(is_active), 사용자 계정은 상태기계+지연 하드삭제(status+hard_delete_at), 일반 레코드는 deleted_at. 인정할 부채: deletedAt 재선언 섀도잉(Report/Inquiry), NOT NULL 오설계 3건의 반복 — 컨벤션 문서화(CLAUDE.md)가 늦었던 것이 원인이며 지금은 성문화됨.

---
*저자 경계 요약: TSID 전환·ID String화·FK 드롭·SoftDeletableEntity 계층·musicians partial index 커밋은 monte-kim [확인]. deleted_at 완화 3건(inquiries/faqs/reports)과 owners partial index는 2-say(Sehee) — 맥락 전용 [확인].*
