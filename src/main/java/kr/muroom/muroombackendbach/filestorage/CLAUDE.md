# Muroom File Storage System V2

## 1. 설계 철학

- **정책 기반 경로**: S3 경로 문자열(`temp/`, `draft/` 등) 하드코딩 금지. 모든 경로는 `FileStorageLocation`을 통해서만 생성
- **물리/비즈니스 격리**: AWS SDK 직접 통신은 `S3Executor`만 담당. `FileStorageService`는 정책 결정만
- **버킷 레벨 보안 분리**: PUBLIC(외부 공개) / PRIVATE(Presigned URL 전용)

## 2. 도메인 모델

### BucketType

- `PUBLIC`: 외부 인터넷에서 직접 접근 가능
- `PRIVATE`: 외부 접근 차단, 반드시 Presigned URL을 통해서만 접근 가능

### FileStorageLocation

| 위치                  | 버킷      | 경로 접두어                | 용도                        |
|---------------------|---------|-----------------------|---------------------------|
| `PUBLIC_PERMANENT`  | PUBLIC  | `""`                  | 정식 등록된 공용 이미지             |
| `PUBLIC_TEMP`       | PUBLIC  | `temp/`               | 공용 이미지 업로드 직후 임시 상태       |
| `PUBLIC_TRASH`      | PUBLIC  | `deletion-scheduled/` | 공용 파일 소프트 딜리트 대상          |
| `PRIVATE_PERMANENT` | PRIVATE | `""`                  | 정식 등록된 비공개 데이터 (예: 문의 내역) |
| `PRIVATE_TEMP`      | PRIVATE | `temp/`               | 비공개 데이터 업로드 직후 임시 상태      |
| `PRIVATE_DRAFT`     | PRIVATE | `draft/`              | 스튜디오 임시 저장본 이미지 전용        |
| `PRIVATE_REPORT`    | PRIVATE | `snapshot/report/`    | 신고 증거 스냅샷 전용              |
| `PRIVATE_TRASH`     | PRIVATE | `deletion-scheduled/` | 비공개 파일 소프트 딜리트 대상         |

**FileStorageLocation 내부 메서드**

- `getTrashLocation()`: BucketType에 따라 `PUBLIC_TRASH` 또는 `PRIVATE_TRASH` 자동 반환
- `extractPureFileName(key)`: 경로 접두어 제거 후 순수 파일명(UUID 포함) 추출
- `generateFullKey(purePath)`: 정책 접두어 + 파일명으로 S3 Full Key 생성

## 3. 모듈 구조

### S3Executor (Infrastructure)

S3 물리 통신 전담. 비즈니스 레이어에서 직접 호출 금지.

- `copy(sourceBucket, sourceKey, destinationBucket, destinationKey)`: 버킷 내/간 객체 복사
- `hardDelete(bucket, key)`: 영구 삭제 — `move` 내부에서만 호출
- `presignUploadUrl(bucket, key, contentType, duration)`: PUT 업로드용 Presigned URL 발급
- `presignViewUrl(bucket, key, duration)`: GET 조회용 Presigned URL 발급 (30분)
- `getPublicUrl(bucket, key)`: PUBLIC 버킷 고정 접근 URL 반환

### FileStorageService (Application)

비즈니스 로직의 유일한 파일 인터페이스.

- `getUploadUrl(sourceLocation, request)`: PUT URL + 서버 관리용 Key 반환
- `getViewUrl(key, sourceLocation)`: PUBLIC → 고정 URL, PRIVATE → 10분 Presigned URL
- `move(key, sourceLocation, destinationLocation)`: 정책 공간 간 이동 (Copy → 원본 HardDelete), Cross-Bucket 자동 처리
- `softDelete(key, sourceLocation)`: TrashLocation으로 이동
- `copyToReportSnapshot(key, sourceLocation, domain)`: 원본 유지 + PRIVATE_REPORT로 복사

## 4. 강제 운영 규칙

### 규칙 1: HardDelete 직접 호출 금지

```java
// ❌ 절대 금지 — 비즈니스 서비스에서 S3Executor 직접 호출
s3Executor.hardDelete(bucket, key);

// ✅ 반드시 FileStorageService.softDelete 사용
fileStorageService.

softDelete(key, FileStorageLocation.PUBLIC_PERMANENT);
```

### 규칙 2: 드래프트 → 정식 등록 시 반드시 move 사용

```java
// ❌ 금지 — PRIVATE_DRAFT Key를 그대로 DB에 저장
studio.setImageKey("draft/uuid.jpg");

// ✅ 반드시 move로 PUBLIC_PERMANENT로 이동 후 저장
String permanentKey = fileStorageService.move(draftKey, FileStorageLocation.PRIVATE_DRAFT, FileStorageLocation.PUBLIC_PERMANENT);
studio.

setImageKey(permanentKey);
```

### 규칙 3: Key 저장 및 조회 원칙

```java
// ✅ DB에는 접두어 포함 Full Key 저장
String fullKey = "temp/550e8400-e29b-41d4-a716-446655440000.jpg";
imageEntity.

setKey(fullKey); // 이대로 저장

// ✅ 조회 시점에 getViewUrl로 변환
String url = fileStorageService.getViewUrl(imageEntity.getKey(), FileStorageLocation.PUBLIC_PERMANENT);
```
