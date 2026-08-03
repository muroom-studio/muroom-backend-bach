# 03. 딥다이브: S3 파일 스토리지 정책 엔진과 Draft 임시저장

> 작성일: 2026-08-02 · 근거: `docs/dossier/raw/studio-filestorage.md`, `02-history.md`, `filestorage/CLAUDE.md`, git 고고학(develop + feature/studio)
> 표기: **[확인]** = 커밋/파일 근거 병기, **[추론]** = 정황 추론. 이 영역의 주 저자는 monte-kim이며, 예외(2-say 기여)는 본문에 명시.

## 1. 요약

브라우저가 S3에 직접 PUT하는 presigned URL 방식을 서비스 첫 커밋(베타, 2025-11-09)부터 채택했고, 서버 경유 업로드는 한 번도 존재한 적이 없다.
5개월에 걸쳐 "단일 버킷 + 도메인 서비스별 수동 조립" → "PUBLIC/PRIVATE 이중 버킷 + temp 승격 라이프사이클"(PR #50) → "enum 정책 테이블 + SDK 격리 계층"(70a1ef7)으로 3단계 진화했다.
핵심 설계는 `FileStorageLocation` enum이 버킷·프리픽스 정책을 전담하고 `S3Executor`만 AWS SDK를 만지는 구조이며, 고아 파일은 앱 레벨 softDelete와 Terraform S3 라이프사이클(7일)로 이중 방어한다.
Studio draft(임시저장)는 8단계 위저드 전체를 단일 JSONB 블롭 + TTL 3일로 저장하고, 이미지 정리는 `extractAllImageKeys()` 단일 진입점으로 수렴시켰다.
단, `@EnableScheduling` 부재로 만료 스케줄러가 실행되지 않고, draft→정식 등록 승격 경로가 미구현인 등 feature/studio 브랜치는 미완 상태다(2026-03-28 이후 커밋 없음).

## 2. 현재 설계

### 2.1 FileStorageLocation — enum 정책 테이블 [확인: `filestorage/domain/FileStorageLocation.java`]

| 상수 | 버킷 | prefix | 용도 |
|---|---|---|---|
| `PUBLIC_PERMANENT` | PUBLIC | `""` | 정식 등록된 공용 이미지 |
| `PUBLIC_TEMP` | PUBLIC | `temp/` | 업로드 직후 임시 상태 |
| `PUBLIC_TRASH` | PUBLIC | `deletion-scheduled/` | 소프트 딜리트 대상 |
| `PRIVATE_PERMANENT` | PRIVATE | `""` | 비공개 정식 데이터 (문의 첨부 등) |
| `PRIVATE_TEMP` | PRIVATE | `temp/` | 비공개 임시 상태 |
| `PRIVATE_DRAFT` | PRIVATE | `draft/` | 스튜디오 임시저장 이미지 전용 |
| `PRIVATE_REPORT` | PRIVATE | `snapshot/report/` | 신고 증거 스냅샷 |
| `PRIVATE_TRASH` | PRIVATE | `deletion-scheduled/` | 비공개 소프트 딜리트 대상 |

enum 자체 메서드: `getTrashLocation()`(버킷 타입에 맞는 TRASH 자동 선택), `extractPureFileName(key)`(prefix 제거), `generateFullKey(purePath)`(prefix 부착). 즉 "경로 문자열을 조립하는 지식"이 enum 밖으로 새지 않는다. [확인] grep 기준 S3 경로 문자열 하드코딩 위반 0건 (raw §설계 관찰).

한계: 크기·콘텐츠타입 정책은 enum에 없다. contentType은 클라이언트 선언값을 그대로 서명하고, 파일 개수/형식 제약은 호출측 DTO 빈밸리데이션이 전부다. [확인: raw §3]

### 2.2 FileStorageService — 얇은 정책 레이어 (공개 API 5개) [확인: 70a1ef7 이후 `application/FileStorageService.java`]

- `getUploadUrl(location, FileUploadRequest)` — 키 = `prefix + domainDirectory + "/" + UUID + "-" + fileName`, presigned PUT URL + 서버 관리용 key 반환
- `getViewUrl(key, location)` — PUBLIC → 고정 URL, PRIVATE → presigned GET
- `move(key, src, dst)` — 정책 공간 간 이동. Cross-bucket 자동 처리
- `softDelete(key, src)` — `move(src → src.getTrashLocation())`의 별칭
- `copyToReportSnapshot(key, src, domain)` — 원본 유지 + PRIVATE_REPORT로 복사 (신고 증거 보존)

`FileUploadRequest`는 인터페이스(getFileName/getContentType/getDomainDirectory)로, 각 도메인의 요청 DTO가 구현해 `studios/main` 같은 디렉토리를 스스로 결정한다.

### 2.3 S3Executor — SDK 격리 [확인: `filestorage/infrastructure/S3Executor.java`, 70a1ef7에서 신설(+97라인)]

`copy` / `hardDelete` / `presignUploadUrl` / `presignViewUrl` / `getPublicUrl` 5개만 노출. AWS SDK v2 타입(S3Client, S3Presigner)이 이 클래스 밖으로 나가지 않는다. 모듈 CLAUDE.md의 강제 규칙: 비즈니스 서비스에서 `s3Executor.hardDelete` 직접 호출 금지 — hardDelete는 `move` 내부에서만 호출된다.

### 2.4 버킷 분리와 이중 방어

- **PUBLIC**(외부 직접 접근) / **PRIVATE**(presigned GET 전용) 버킷 분리. [확인] 이 분리는 PR #50(2025-12-24)에서 도입 — 그 이전 `FileStorageService`는 단일 `cloud.aws.s3.bucket` 설정만 사용 (`git show e4904da^1:...FileStorageService.java`).
- **고아 파일 이중 방어**: (1) 앱 레벨 — 엔티티 저장/수정/삭제 시 명시적 move/softDelete, (2) 인프라 레벨 — Terraform S3 라이프사이클이 `temp/`·`draft/`·`deletion-scheduled/` prefix를 prod 7일/dev 1일에 자동 만료. [확인: e859aab의 `infra/s3.tf` diff — `prod-private-draft-cleanup` 규칙 prefix `draft/`, days 7]

## 3. 코드 수준 동작

### 3.1 presigned PUT 발급 흐름 [확인: FileStorageService.getUploadUrl]

1. 도메인 컨트롤러/서비스가 자기 위치 정책(예: `PUBLIC_TEMP`, `PRIVATE_DRAFT`)과 DTO를 넘김
2. `resolveBucket(location.getBucketType())`으로 버킷 결정
3. 키 생성: `String.format("%s%s/%s-%s", prefix, domainDirectory, UUID, fileName)`
4. `s3Executor.presignUploadUrl(bucket, key, contentType, expiration)` — 단일 `cloud.aws.s3.presign.expiration` 설정 공유 (prod 5분, dev/local 10분 [확인: application-{prod,dev,local}.yml])
5. 클라이언트는 URL로 직접 PUT, 서버는 key만 DB에 저장. **서버 경유 멀티파트 업로드 경로는 리포 역사상 존재한 적 없음** [확인: `git log -S "presign"` 최초 히트가 최초 기능 커밋 1c0075f]

### 3.2 move = copy + hardDelete [확인: FileStorageService.move]

S3에 원자적 move가 없으므로 `copy(srcBucket, key, dstBucket, dstKey)` 후 `hardDelete(srcBucket, key)`. 비멱등·비보상: hardDelete 실패 시 원본이 고아로 남고, 이후 DB 트랜잭션이 롤백되면 S3는 이미 이동된 상태로 남는다(6장 참고). `softDelete`는 move의 목적지를 TRASH로 고정한 것 — "삭제"조차 실제로는 `deletion-scheduled/`로의 이동이고, 진짜 삭제는 라이프사이클 규칙이 7일 뒤 수행한다.

### 3.3 temp→permanent 승격 인벤토리 [확인: raw §3 표]

| 호출자 | 업로드 위치 | 승격 | 삭제 |
|---|---|---|---|
| StudioCommandService | PUBLIC_TEMP | move→PERMANENT (create/update) | softDelete |
| OwnerStudioDraftController | PRIVATE_DRAFT | **없음 (미구현)** | softDelete(draft 정리) |
| StudioBoastService | PUBLIC_TEMP | move | softDelete |
| Inquiry/InquiryReplyService | PRIVATE_TEMP | move→PRIVATE_PERMANENT | — |
| StudioBoastReportTargetHandler | — | copyToReportSnapshot | — |

승격은 항상 엔티티를 저장하는 서비스 메서드 안에서 **명시적·수동**으로 일어난다. 엔티티 리스너, 아웃박스, `@TransactionalEventListener` 같은 자동화 장치는 없다. [확인: raw §3]

### 3.4 Draft: JSONB + TTL [확인: b077677, V26030801__create_studio_drafts.sql]

- 테이블: `studio_draft_data JSONB NOT NULL` 단일 컬럼에 8단계 위저드 전체 상태를 블롭 저장. 리스팅용으로 `step`, `studio_name`만 컬럼 승격. owner_id/expires_at 인덱스, FK 없음.
- TTL: create 시 `expiresAt = now + 3일`, update마다 3일 재연장. [확인: StudioDraftCommandService]
- 소유권: `findByIdAndOwnerId` 복합 파인더로 스코핑 (별도 권한 검사 코드 없음).
- 삭제: 전 이미지 키 softDelete 후 **하드 delete** — StudioDraft는 AuditableEntity 상속이라 소프트삭제 대상이 아님.

### 3.5 extractAllImageKeys — 정리의 단일 진입점 [확인: 3151cf4 diff]

`StudioDraftData.extractAllImageKeys()`가 main/building/room/blueprint/commonOption/individualOption 6개 필드의 키를 null-safe로 합산한다. 이 메서드 하나가 세 곳에서 재사용된다:

- **update**: 구 키 리스트 − 신 키 셋 차집합 → 고아 키만 `softDelete(PRIVATE_DRAFT)` (diff 기반 정리)
- **delete**: 전 키 softDelete 후 엔티티 하드 삭제
- **deleteExpiredDrafts**: `findAllByExpiresAtBefore(now)` → 각 draft의 전 키 정리 → `deleteAll`

단일 진입점이라 위저드에 이미지 필드가 추가되면 이 메서드도 반드시 갱신해야 한다 — 유일한 단일 실패점(SPOF). [확인: raw §설계 관찰]

## 4. 진화 과정

전 단계 monte-kim 저자 (예외는 명시).

### V0 — 베타 시절: 단일 버킷 presigned (2025-11-09, `1c0075f`, PR #1)

최초 기능 커밋부터 presigned PUT/GET이었다. [확인: `git show 1c0075f` — `common/config/S3Config.java`, `common/domain/FileUploadType.java` 포함]
`git show e4904da^1:...FileStorageService.java`로 확인한 PR #50 직전 형태:

- 단일 버킷(`cloud.aws.s3.bucket`), `S3Presigner`만 주입. `generatePresignedPutUrl(fileName, domain, contentType)` / `generatePresignedGetUrl(fileKey)` 2개가 전부.
- temp/ 승격도, 삭제 정책도 없음. 키 = `domain/UUID-sanitizedFileName`.
- **일괄 발급은 도메인 서비스 몫**: beta `RegistrationService.generatePresignedPutUrls`가 파일 요청 리스트를 돌며 URL 리스트(`GeneratePresignedUrlsPutResponse`)로 반환 [확인: `git show 1c0075f:...RegistrationService.java`]. 콘텐츠타입 화이트리스트(`ALLOWED_CONTENT_TYPES`)도 도메인 서비스에 하드코딩.

즉 V0의 문제는 "presigned 자체"가 아니라 **정책(경로·검증·일괄성)이 전부 호출측에 흩어져 있던 것**이다.

### V1 — PR #50: "S3 presigned url 개별 제공으로 구조 개선" (2025-12-24, `e4904da` 머지)

boast 기능 개발(12-22 `246e3c2`~)과 함께 FileStorageService를 전면 개편 [확인: PR #50 diffstat — FileStorageService.java 186라인 변경, FileUploadRequest 35라인, FileErrorCode +5]:

- **일괄 → 개별**: 리스트를 받아 URL 묶음을 돌려주던 방식에서, 파일 1건당 1회 발급(`FileUploadRequest` 인터페이스를 각 도메인 DTO가 구현)으로 전환
- **PUBLIC/PRIVATE 이중 버킷 도입** + `temp/`, `deletion-scheduled/` prefix 상수 + `movePublicFileFromTempToPermanent` 류 승격 메서드 등장
- 다만 이 시점의 API는 **유스케이스별 메서드 나열**이었다: `generatePresignedPutUrlForPublic/ForPrivate`, `movePublic.../movePrivate...`, `copyPublic/PrivateFileToReportSnapshot` + `Consumer<String> contentTypeValidator` 파라미터 [확인: `git show 70a1ef7^:...FileStorageService.java`]
- `f6af6da`(12-24) "fix: file key temp 제거 안 됨 해결 및 문서화 추가" — 승격 시 temp/ prefix가 키에서 벗겨지지 않던 버그 수정 + 최초 문서화 [확인: diffstat]
- 팀원 기여 2건 [확인]: `c6ce091`(2-say, 12-20) 문의용 presigned URL API, `52f840e`(2-say, 12-27) 신고 스냅샷 복사 로직 — filestorage가 monte 단독 소유는 아니었음
- 재미있는 흔적: 이 시기 javadoc에 "예: 트레이너 프로필 사진", "예: 자격증 파일"이 남아 있다 [확인: 70a1ef7^ 파일] — 다른 프로젝트(피트니스 도메인) 코드를 참고해 이식한 흔적 [추론]

### V2 — 70a1ef7: 정책 엔진 리팩토링 (2026-03-08, feature/studio)

"refactor: s3 파일 관리 모듈 리팩토링" [확인: `git show 70a1ef7 --stat` — 11파일, +236/−268 = **순감 32라인**]:

- 신설: `domain/BucketType`(+18), `domain/FileStorageLocation`(+40), `infrastructure/S3Executor`(+97)
- `FileStorageService` 281라인 변경 — Public/Private 쌍으로 늘어나던 유스케이스 메서드들을 `FileStorageLocation` 파라미터를 받는 **제네릭 5메서드**로 붕괴시킴
- 호출측 6개 파일(inquiry 3, studio 2, studioboasting 1, report 1) 동시 수정 — 전 호출자를 새 API로 일괄 이행
- 직후 `a6709fa`(같은 날) "claude context 추가" — 모듈 CLAUDE.md(설계 철학 문서) 작성

기능이 늘었는데 코드가 줄어든 리팩토링: 위치가 2(버킷)×N(용도) 조합으로 늘어날수록 메서드가 배로 늘던 구조를, enum 한 줄 추가로 끝나는 구조로 바꿨다. 실제로 이후 `PRIVATE_DRAFT`는 enum 상수 1개 추가로 편입됐다.

### V3 — draft 3부작 (feature/studio, monte-kim)

| 커밋 | 날짜 | 내용 [확인: 각 --stat] |
|---|---|---|
| `b077677` | 2026-03-09 | **임시저장 본체** (+868라인): StudioDraft 엔티티(JSONB), V26030801 DDL, CRUD 서비스/컨트롤러/문서, 222라인짜리 StudioDraftSaveRequest, room 도메인 valueobject(RoomInfo·DiscountBenefit) 신설 |
| `3151cf4` | 2026-03-28 | **이미지 편입** (+135라인): `extractAllImageKeys()` 추가, update의 diff 정리·delete의 전체 정리·`deleteExpiredDrafts` 구현, draft 전용 presigned-url 엔드포인트, `StudioDraftExpirationScheduler` 파일 신설(cron 03:00 KST) |
| `e859aab` | 2026-03-28 | **인프라 마감** (+36라인): Terraform `draft/` 라이프사이클 규칙(prod 7일/dev 1일), 요청 DTO 정돈 |

주의: e859aab의 커밋 메시지는 "만료 스케줄러 추가"지만, 스케줄러 클래스 자체는 3151cf4에서 추가됐고 e859aab의 실체는 Terraform 라이프사이클이다 [확인: 두 커밋 diff 대조]. 그리고 이것이 이 리포의 마지막 커밋이다(2026-03-28, 이후 4개월 공백 [확인: 02-history §2]).

## 5. 대안의 흔적

- **서버 경유 업로드**: 흔적 없음. 첫 커밋부터 presigned 직행 [확인: 3.1]. 서버가 파일 바이트를 만진 적이 한 번도 없어 인스턴스 메모리/대역폭과 무관한 업로드 경로가 유지됨.
- **리팩토링 전 구조 (기각된 형태)**: "버킷별 × 유스케이스별 메서드 폭발" — `...ForPublic/ForPrivate` 쌍 + prefix 문자열 상수가 서비스 내부에, 콘텐츠타입 검증은 호출자가 `Consumer<String>`로 주입 [확인: 70a1ef7^]. 위치가 하나 늘 때마다 메서드 2~4개가 늘어나는 구조여서 enum 정책 테이블로 대체됨.
- **일괄 URL 발급 (V0)**: 도메인 서비스가 리스트를 순회하며 URL 묶음을 리턴 → PR #50에서 개별 발급으로 전환. 프론트가 파일 선택 시점마다 개별 요청하는 UX와 정합 [추론 — PR 제목 "개별 제공으로 구조 개선" 외 상세 논의 기록 미확보].
- **draft: JSONB vs 섀도 릴레이셔널 스키마**: 정식 스튜디오는 8개 이상 테이블(studio/price/building/room/option/image/...)로 정규화돼 있으므로, "섀도 테이블" 대안은 draft용 테이블 세트를 통째로 복제하고 부분 저장(모든 필드 nullable)을 허용해야 했을 것. 대신 단일 JSONB 블롭 + step 컬럼을 택해 위저드 스키마 진화에 유연하고, 검증은 최종 등록 시점으로 미룸 [확인: 구조는 V26030801 / 트레이드오프 평가는 raw §설계 관찰]. 반대급부로 draft 데이터에 대한 SQL 레벨 질의·제약이 불가능하고 `extractAllImageKeys`가 SPOF가 됨.
- **자동 승격 장치(리스너/이벤트) 미채택**: 승격이 항상 서비스 메서드 안 수동 호출 — 명시성을 택한 것으로 보이나 채택/기각 논의 기록은 없음 [추론].

## 6. 성능 / 제약 / 미완

각 항목: 현상 → 영향 → 개선안 한 줄.

1. **S3–DB 원자성 부재** [확인: raw §3] — move(copy+hardDelete)가 DB 트랜잭션 안에서 실행되며 보상 로직 없음 → 트랜잭션 롤백 시 S3만 이동 완료된 불일치, hardDelete 실패 시 원본 고아 → 승격을 커밋 후(AFTER_COMMIT 이벤트)로 미루고, 실패 잔여물은 라이프사이클 규칙이 이미 수거하므로 "DB 커밋이 진실, S3는 최종 일관"으로 정리.
2. **검증 전 move** [확인: raw §1 — StudioCommandService.createStudio는 썸네일/블루프린트 검증(5단계)보다 앞서 S3 move 수행] → 검증 실패 시 이미 PERMANENT로 승격된 고아 발생(temp/ 라이프사이클 보호 범위 밖) → 검증 → DB 저장 → move 순서로 재배열.
3. **`@EnableScheduling` 부재** [확인: 소스 전체 grep 0건, raw에서 재검증] — StudioDraftExpirationScheduler(03:00)와 withdrawal HardDeleteScheduler(04:00) 모두 데드코드 → draft DB 행이 영구 잔존(S3 쪽만 라이프사이클로 정리됨) → 메인 클래스에 `@EnableScheduling` 한 줄 + 다중 인스턴스 대비 ShedLock.
4. **draft→정식 승격 미구현** [확인: StudioCommandService는 StudioDraft 미참조, PRIVATE_DRAFT→PUBLIC_PERMANENT move 코드 부재] — 모듈 CLAUDE.md "규칙 2"가 규정한 흐름이 코드에 없음. owner용 스튜디오 등록 API 자체가 아직 없음 → 임시저장은 되지만 제출이 안 되는 반쪽 기능 → owner 등록 커맨드에서 draft 로드 + 키 일괄 move + draft 삭제를 한 유스케이스로 구현.
5. **draft 바디 `@Valid` 누락** [확인: OwnerStudioDraftController create/update — presigned-url만 `@Validated`] → StudioDraftSaveRequest의 `@Min/@Max/@Size` 전부 비활성. 부분 저장 특성상 의도일 수도 있으나 그렇다면 애노테이션 자체가 죽은 코드 [추론: 의도 불명] → `@Valid` 추가 또는 제약 애노테이션 제거로 의도를 코드에 일치.
6. **presign TTL 문서 모순** [확인] — 모듈 CLAUDE.md는 GET 30분(S3Executor 절)과 10분(FileStorageService 절)을 동시에 주장하나, 코드는 단일 설정값이고 실값은 prod 5분/dev·local 10분(application-*.yml) → 문서를 "환경별 단일 설정" 한 줄로 정정.
7. **콘텐츠타입/크기 서버 강제 없음** [확인: raw §3] — 클라이언트 선언 contentType 그대로 서명, 크기 제한 없음 → 위장 업로드(예: 대용량, 비이미지) 가능 → presign 조건(content-length-range)·버킷 정책, 또는 서명 전 서버 화이트리스트(V0에는 있었으나 리팩토링에서 소실된 검증).
8. **draft 상세가 raw S3 키 반환** [확인: raw §2 — getViewUrl 미호출] → PRIVATE 버킷이라 클라이언트가 이미지를 렌더링할 수 없음 → 상세 응답 조립 시 `getViewUrl(key, PRIVATE_DRAFT)` 호출.

## 7. 면접 예상 질문 씨앗

1. **"S3와 DB의 정합성은 어떻게 보장하나?"** — 원자성은 포기하고 방향성 있는 최종 일관성으로 설계: 실패 잔여물이 반드시 temp/·draft/·trash prefix에 남도록 상태 기계를 짜고, Terraform 라이프사이클(7일)이 최후 수거자. 현재 한계(트랜잭션 내 move, 보상 없음)와 AFTER_COMMIT 개선안까지 말할 수 있어야 함.
2. **"왜 서버를 거치지 않고 presigned URL인가?"** — 파일 바이트가 서버를 안 지나므로 소형 인스턴스(비용 최적화 기조)에서도 업로드 대역폭·메모리 무관. 대가로 contentType/크기 검증권을 일부 상실 → 서명 조건·버킷 정책으로 보완하는 이야기.
3. **"파일 위치 정책을 enum으로 만든 이유는?"** — 리팩토링 전 '버킷×유스케이스 메서드 폭발'(70a1ef7 diff, 순감 32라인으로 기능 확장)을 근거로: 새 위치 추가 비용이 메서드 N개 → enum 상수 1개로 줄었고, PRIVATE_DRAFT 편입이 실증 사례.
4. **"임시저장을 왜 JSONB 블롭으로 했나? 정규화 안 하고?"** — draft는 질의 대상이 아니라 '이어쓰기 스냅샷'이므로 스키마 유연성 > 질의 능력. 섀도 테이블 대안의 비용(전 테이블 복제 + all-nullable), 대신 리스팅용 컬럼(step, studio_name)만 승격한 절충, extractAllImageKeys SPOF 리스크 인지.
5. **"임시 파일 고아는 어떻게 청소되나?"** — 3중 구조: update 시 diff 기반 softDelete(앱), 만료 draft 스케줄러(앱, 단 @EnableScheduling 부재로 현재 미동작 — 스스로 발견한 결함으로 제시 가능), S3 라이프사이클(인프라). '앱이 실패해도 인프라가 수거한다'는 이중 방어 논리.
