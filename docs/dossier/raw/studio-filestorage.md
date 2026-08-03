# [RAW] 스튜디오 도메인 & 파일스토리지 조사 결과 (에이전트 원본 + 검증, 2026-08-02)

> 핵심 주장 중 `@EnableScheduling` 부재는 오케스트레이터가 grep으로 재검증 [확인].

## 1. 스튜디오 등록 흐름 (admin 전용 쓰기 경로)

**사장님용 스튜디오 생성 엔드포인트가 없다.** 생성/수정/삭제는 `admin/studio/presentation/AdminStudioController.java`(`/api/admin/studios`, `@PreAuthorize` 없음)뿐.

- 요청 DTO: owner를 **전화번호로** 식별(`ownerPhoneNumber`). 이미지 키 상한: main 1–3, building ≤4, room ≤20, blueprint 필수, 옵션 각 ≤10.
- `StudioCommandService`(`studio/application/command/`) — **클래스 레벨 `@Transactional`**(presigned-url 메서드까지 rw 트랜잭션).

### `POST /api/admin/studios` 시퀀스 (createStudio, `StudioCommandService.java:79-148`)
1. owner 전화번호 조회 (`OwnerService.findByPhoneNumberOrThrowException`)
2. **트랜잭션 안에서 외부 HTTP**: `MapGeocodingService.getPointFromAddress` — juso.go.kr Feign 2연쇄(검색→좌표) + GRS80→WGS84 변환 → JTS Point
3. **temp→permanent 승격**: 카테고리별 전 키에 `fileStorageService.move(key, PUBLIC_TEMP, PUBLIC_PERMANENT)` (`:524-586`) — **썸네일/블루프린트 검증(5단계)보다 먼저 S3 이동이 일어남**
4. 썸네일 = MAIN sequence==1, 블루프린트 = 첫 BLUEPRINT; 없으면 THUMBNAIL/BLUEPRINT_IMAGE_NOT_FOUND
5. Studio 저장 (TSID, geography Point, viewCount 0)
6. StudioPrice(@MapsId, min<=max 검증) → StudioBuildingInfo(@MapsId, 화장실 상세 필수 규칙) → Rooms(`roomRepository.saveAll`, plain studioId) → StudioOption(코드 조회, **미지 코드 무시**) → StudioForbiddenInstrument(동일) → StudioImage → SubwayStationNearbyStudio(**클라이언트가 고른 역 + sequence**, 역마다 findById N+1)
7. `savedStudio.getId()` 반환(Long — draft 응답의 String과 비일관), 커밋

### update/delete
- `updateStudio` — diff 기반. 주소는 **매 업데이트마다 재지오코딩**. 룸 diff는 **이름 키** (`Collectors.toMap(Room::getName…)` — 중복 이름 시 IllegalStateException). `imageKeys` null이면 전체 이미지 삭제. 새 키 move, 제거 키 softDelete.
- `deleteStudio` — S3 softDelete 먼저, 그다음 서브엔티티 + studio JPA 소프트삭제.

## 2. Studio Draft (임시저장)

커밋 궤적: `b077677`(기능) → `3151c b79`(이미지 업로드/정리) → `e859aab`(만료 스케줄러). DDL: `V26030801__create_studio_drafts.sql` — `studio_draft_data JSONB NOT NULL`, owner_id/expires_at 인덱스, FK 없음.

- **설계: 8단계 위저드 전체를 단일 JSONB 블롭 + TTL(3일)로.** `step`과 `studio_name`만 리스팅용 컬럼으로 승격. `StudioDraftData.extractAllImageKeys()`(`:34-55`)가 모든 이미지 정리의 단일 진입점.
- 컨트롤러 `OwnerStudioDraftController`(`/api/v1/owner/studios/drafts`, 전 메서드 `hasRole('OWNER')`, `@CurrentUserId`):
  - `POST /presigned-url` → **컨트롤러가 FileStorageService 직접 호출**(앱 서비스 우회), `PRIVATE_DRAFT` 위치
  - CRUD: 생성/목록(`findAllByOwnerIdOrderByUpdatedAtDesc`)/단건/수정/삭제 — 소유권은 `findByIdAndOwnerId` 복합 파인더로 스코핑
  - **[버그성] create/update 바디에 `@Valid` 없음** (`:54`, `:95`) → `@Min/@Max/@Size` 제약 전부 비활성 (presigned-url만 `@Validated`)
  - 상세 응답이 **raw S3 키 반환** (getViewUrl 미호출) — PRIVATE 버킷이라 presigned GET 없이는 접근 불가
- `StudioDraftCommandService`:
  - create: `expiresAt = now+3d`, studioName 비정규화
  - update: 구/신 키 diff → 고아 키 `softDelete(PRIVATE_DRAFT)` (**DB 트랜잭션 안 S3 부수효과**), `expiresAt` 갱신(3일 연장), 더티체킹 저장
  - delete: 전 키 softDelete 후 **하드 delete** (draft는 AuditableEntity — 소프트삭제 대상 아님)
  - `deleteExpiredDrafts`: `findAllByExpiresAtBefore(now)` → 키 정리 → deleteAll
- **만료 스케줄러**: `studio/job/StudioDraftExpirationScheduler.java` — `@Scheduled(cron="0 0 3 * * *", zone="Asia/Seoul")`. **그러나 `@EnableScheduling`이 소스 트리 어디에도 없음** [확인 — grep] → 이 스케줄러와 `withdrawal/job/HardDeleteScheduler`(04:00) 모두 **런타임에 실행되지 않는 데드코드**. 만료 정리는 사실상 S3 라이프사이클(draft/ 7일)에만 의존, DB 행은 안 지워짐.
- **draft → 정식 스튜디오 승격 경로 없음** [확인 — grep]: StudioCommandService는 StudioDraft를 전혀 참조 안 하고 PUBLIC_TEMP 키만 받음. PRIVATE_DRAFT → PUBLIC_PERMANENT 이동 코드 부재. filestorage/CLAUDE.md의 "규칙 2"가 규정한 흐름이 미구현.

## 3. filestorage 모듈 — 정책 엔진

철학(모듈 CLAUDE.md): S3 경로 문자열 하드코딩 금지, 모든 경로는 `FileStorageLocation`으로만, SDK는 `S3Executor`에 격리, PUBLIC/PRIVATE 버킷 분리.

### `FileStorageLocation` — enum 정책 테이블 (bucket + prefix)

| 상수 | 버킷 | prefix |
|---|---|---|
| PUBLIC_PERMANENT / PRIVATE_PERMANENT | 각 | `""` |
| PUBLIC_TEMP / PRIVATE_TEMP | 각 | `temp/` |
| PUBLIC_TRASH / PRIVATE_TRASH | 각 | `deletion-scheduled/` |
| PRIVATE_DRAFT | PRIVATE | `draft/` |
| PRIVATE_REPORT | PRIVATE | `snapshot/report/` |

메서드: `getTrashLocation()`(버킷 인지), `extractPureFileName`, `generateFullKey`. **크기/콘텐츠타입 규칙은 enum에 없음** — 호출측 DTO 빈밸리데이션이 전부, contentType은 클라이언트 선언값 그대로 서명.

### `FileStorageService` 공개 API
- `getUploadUrl(location, FileUploadRequest)` — 키 = `prefix + domainDirectory + "/" + UUID + "-" + fileName`; presigned PUT (**서버 경유 멀티파트 업로드 경로 없음** — 전부 브라우저 직접 PUT)
- `getViewUrl(key, location)` — PUBLIC → 고정 URL, PRIVATE → presigned GET (PUT과 같은 TTL 설정 공유; 모듈 CLAUDE.md는 30분/10분으로 자체 모순, 코드는 단일 설정값)
- `move(key, src, dst)` — S3에 move가 없으므로 **copy + hardDelete**. 비멱등·비보상: hardDelete 실패 시 원본 고아, DB 롤백 시 S3와 불일치
- `softDelete(key, src)` = `move(src → trash)`; `copyToReportSnapshot`(복사만 — 신고 증거 스냅샷)
- `FileUploadRequest` 인터페이스(getFileName/getContentType/getDomainDirectory)를 각 도메인 DTO가 구현 → `studios/<category>` 등 디렉토리 결정

### 승격/정리 호출 인벤토리
| 호출자 | 업로드 위치 | 승격 | 삭제 |
|---|---|---|---|
| StudioCommandService | PUBLIC_TEMP | move→PERMANENT (create/update) | softDelete |
| OwnerStudioDraftController | PRIVATE_DRAFT | **없음** | softDelete(draft 정리) |
| StudioBoastService | PUBLIC_TEMP | move | softDelete |
| Inquiry/InquiryReplyService | PRIVATE_TEMP | move→PRIVATE_PERMANENT | — |
| StudioBoastReportTargetHandler | — | copyToReportSnapshot | — |

승격은 항상 **엔티티 저장 서비스 메서드 안에서 명시적·수동** — 엔티티 리스너/아웃박스/`@TransactionalEventListener` 없음. 앱 레벨 정리와 Terraform S3 라이프사이클(temp/·draft/·deletion-scheduled/ 7일)의 **의도적 이중 방어**.

## 4. 스튜디오 조회 측

- `StudioQueryService`(readOnly) `getStudio`: 조회수는 `@Async @Transactional` 별도 스레드/tx(`StudioViewService`). 조인 없이 **엔티티별 1쿼리씩 배치-로드 후 조립**(명시적 N+1 회피 스타일). 역 거리는 **읽기 시점 Haversine**. 
  - [버그성] 주차 위치가 있으면 **상세 조회마다 지오코딩 외부 호출**(`:133-139`); BLUEPRINT 없는 스튜디오에서 `.getFirst()` → NoSuchElementException(`:170-171`)
- `StudioService`(맵/리스트, plain @Transactional — CQRS 분류 밖): 검색 결과 보강은 벌크 맵 조립. `isFavorite`는 map-search는 벌크, **map-list는 행별 Redis 호출(N+1)**.
- `getStudioInfoById(s)` — studioboasting 전용 크로스 모듈 지원 메서드.

## 5. studioboasting 한 단락

뮤지션 UGC "스튜디오 자랑": `/api/v1/studio-boasts` — presigned-url, CRUD, 상세/페이지 목록/my/simple 피드, 좋아요, 신고; 중첩 댓글/대댓글 + 댓글 좋아요 + 댓글 신고. 애그리거트 `StudioBoast`(content, 썸네일 키, **스튜디오명/주소 비정규화**, optional studioId 링크, creatorUserId, **likeCount 비정규화**, 인스타 핸들 + 이벤트 약관 동의 — 이벤트성 기능 흔적). 이미지는 동일한 TEMP→PERMANENT 승격, sequence 0-base(0번이 썸네일). studio 모듈 의존은 `validateStudioIdExists`/`getStudioInfoById(s)`뿐.

## 설계 관찰

- **enum-as-policy-table** (FileStorageLocation) + 얇은 정책 레이어(FileStorageService) + SDK 격리(S3Executor) — "경로 문자열 하드코딩 금지" 규칙이 실제로 전 리포에서 지켜짐 (grep 상 위반 0).
- draft = JSONB 블롭+TTL (섀도 릴레이셔널 스키마 대신) — 위저드 스키마 진화에 유연, 단 extractAllImageKeys가 단일 실패점.
- 소유권 스코핑은 `findByIdAndOwnerId` 파인더 패턴.
- S3 부수효과가 DB 트랜잭션 내부 — 보상 트랜잭션 없음 (질문 대상).

## 미해결 질문 (에이전트 제기, 중요도순)

1. `@EnableScheduling` 부재 — 실제 버그인가, 다른 활성화 경로가 있나? (draft 만료 + 회원 하드삭제 둘 다 영향)
2. draft→studio 승격 미구현 — 다음 마일스톤인가? 어디에 구현 예정? (owner-facing 스튜디오 등록 API 자체가 아직 없음)
3. admin API 인증 부재와 결합 — `/api/admin/studios`가 로그인만으로 열림
4. draft 상세의 raw 키 반환 — 클라이언트 렌더링 방법?
5. draft 바디 `@Valid` 누락 — 의도(부분 저장 특성)인가 실수인가?
6. S3-DB 원자성 부재 + 검증 전 S3 move — 보상 계획?
7. presign TTL 문서 모순(30분 vs 10분)
8. 사이즈/콘텐츠타입 서버 강제 없음 — 버킷 정책/CDN에서 강제?
9. 상세 조회 시 주차장 지오코딩 + blueprint getFirst() 예외 — 인지된 이슈?
10. 룸 이름 diff 키 — 중복 이름 방지 장치 없음
