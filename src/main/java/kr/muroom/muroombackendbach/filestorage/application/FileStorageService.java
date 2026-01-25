package kr.muroom.muroombackendbach.filestorage.application;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.exception.FileErrorCode;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.request.FileUploadRequest;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Service
public class FileStorageService {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;
  private final String privateBucket;
  private final String publicBucket;
  private final Duration expiration;

  private static final String TEMPORARY_FILE_KEY_PREFIX = "temp/";
  private static final String DELETION_SCHEDULED_FILE_KEY_PREFIX = "deletion-scheduled/";

  private static final String SNAPSHOT_REPORT_PREFIX = "snapshot/report/";

  public FileStorageService(
      S3Client s3Client,
      S3Presigner s3Presigner,
      @Value("${cloud.aws.s3.private-bucket-name}") String privateBucket,
      @Value("${cloud.aws.s3.public-bucket-name}") String publicBucket,
      @Value("${cloud.aws.s3.presign.expiration}") Duration expiration
  ) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.privateBucket = privateBucket;
    this.publicBucket = publicBucket;
    this.expiration = expiration;
  }

  /**
   * Public 버킷의 temp 경로로 업로드할 수 있는 Presigned URL을 생성합니다. (예: 트레이너 프로필 사진)
   */
  public GeneratePresignedPutUrlResponse generatePresignedPutUrlForPublic(
      FileUploadRequest fileUploadRequest, Consumer<String> contentTypeValidator) {
    return generatePresignedPutUrl(publicBucket, fileUploadRequest, contentTypeValidator);
  }

  /**
   * Private 버킷의 temp 경로로 업로드할 수 있는 Presigned URL을 생성합니다. (예: 자격증 파일)
   */
  public GeneratePresignedPutUrlResponse generatePresignedPutUrlForPrivate(
      FileUploadRequest fileUploadRequest, Consumer<String> contentTypeValidator) {
    return generatePresignedPutUrl(privateBucket, fileUploadRequest, contentTypeValidator);
  }

  /**
   * Public 버킷의 temp 파일을 영구 경로로 이동시킵니다.
   */
  public String movePublicFileFromTempToPermanent(String tempFileKey) {
    return moveFromTempToPermanent(publicBucket, tempFileKey);
  }

  /**
   * Private 버킷의 temp 파일을 영구 경로로 이동시킵니다.
   */
  public String movePrivateFileFromTempToPermanent(String tempFileKey) {
    return moveFromTempToPermanent(privateBucket, tempFileKey);
  }

  /**
   * Public 버킷 파일을 snapshot/report/{domain}/ 아래로 복사합니다. (원본 유지)
   */
  public String copyPublicFileToReportSnapshot(String domain, String fileKey) {
    return copyFileToReportSnapshot(publicBucket, domain, fileKey);
  }

  /**
   * Private 버킷 파일을 snapshot/report/{domain}/ 아래로 복사합니다. (원본 유지)
   */
  public String copyPrivateFileToReportSnapshot(String domain, String fileKey) {
    return copyFileToReportSnapshot(privateBucket, domain, fileKey);
  }

  public String getPublicFileUrl(String fileKey) {
    GetUrlRequest getUrlRequest = GetUrlRequest.builder()
        .bucket(publicBucket)
        .key(fileKey)
        .build();
    return s3Client.utilities().getUrl(getUrlRequest).toExternalForm();
  }

  public String generatePresignedGetUrlForPrivateFile(String fileKey) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(privateBucket)
        .key(fileKey)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(expiration)
        .getObjectRequest(getObjectRequest)
        .build();

    PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(
        presignRequest);
    return presignedGetObjectRequest.url().toString();
  }

  public void deletePublicFile(String fileKey) {
    softDeleteFile(publicBucket, fileKey);
  }

  public void deletePrivateFile(String fileKey) {
    softDeleteFile(privateBucket, fileKey);
  }

  // --- 내부 헬퍼 메서드 ---
  private GeneratePresignedPutUrlResponse generatePresignedPutUrl(
      String bucket, FileUploadRequest fileUploadRequest, Consumer<String> contentTypeValidator) {
    contentTypeValidator.accept(fileUploadRequest.getContentType());

    String fileName = fileUploadRequest.getFileName();
    String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    String domainDirectory = fileUploadRequest.getDomainDirectory();
    String fileKey = String.format("%s%s/%s-%s",
        TEMPORARY_FILE_KEY_PREFIX, domainDirectory, UUID.randomUUID(), sanitizedFileName
    );

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(fileKey)
        .contentType(fileUploadRequest.getContentType())
        .build();

    PutObjectPresignRequest putObjectPresignRequest = PutObjectPresignRequest.builder()
        .signatureDuration(expiration)
        .putObjectRequest(putObjectRequest)
        .build();

    PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(
        putObjectPresignRequest);

    return GeneratePresignedPutUrlResponse.builder()
        .presignedPutUrl(presignedPutObjectRequest.url().toString())
        .fileKey(fileKey)
        .build();
  }

  private String moveFromTempToPermanent(String bucket, String tempFileKey) {
    if (tempFileKey == null || !tempFileKey.startsWith(TEMPORARY_FILE_KEY_PREFIX)) {
      throw new BusinessException(FileErrorCode.INVALID_TEMP_FILE_KEY);
    }

    String permanentFileKey = tempFileKey.substring(TEMPORARY_FILE_KEY_PREFIX.length());

    CopyObjectRequest copyRequest = CopyObjectRequest.builder()
        .sourceBucket(bucket)
        .sourceKey(tempFileKey)
        .destinationBucket(bucket)
        .destinationKey(permanentFileKey)
        .build();
    try {
      s3Client.copyObject(copyRequest);
    } catch (NoSuchKeyException e) {
      log.warn(e.getMessage());
      throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
    }

    deleteFilePermanently(bucket, tempFileKey);

    return permanentFileKey;
  }

  private String copyFileToReportSnapshot(String bucket, String domain, String fileKey) {
    if (fileKey == null || fileKey.isBlank()) {
      throw new BusinessException(
          FileErrorCode.FILE_NOT_FOUND);
    }
    if (domain == null || domain.isBlank()) {
      throw new BusinessException(FileErrorCode.INVALID_FILE_PATH);
    }

    String normalizedDomain = domain.trim().toLowerCase();

    // 원본 key에서 파일명만 뽑아서 스냅샷 키 생성
    String fileName = extractFileName(fileKey); // 예: abc.png, 123/abc.png -> abc.png
    String snapshotFileKey = String.format("%s%s/%s-%s",
        SNAPSHOT_REPORT_PREFIX,
        normalizedDomain,
        UUID.randomUUID(),
        fileName
    );

    CopyObjectRequest copyRequest = CopyObjectRequest.builder()
        .sourceBucket(bucket)
        .sourceKey(fileKey)
        .destinationBucket(bucket)
        .destinationKey(snapshotFileKey)
        .build();

    try {
      s3Client.copyObject(copyRequest);
    } catch (NoSuchKeyException e) {
      log.warn("Snapshot copy failed - file not found. bucket={}, key={}", bucket, fileKey);
      throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
    }

    return snapshotFileKey;
  }

  private void softDeleteFile(String bucket, String deletionRequestedFileKey) {
    if (deletionRequestedFileKey == null || deletionRequestedFileKey.isEmpty()) {
      return;
    }

    CopyObjectRequest copyRequest = CopyObjectRequest.builder()
        .sourceBucket(bucket)
        .sourceKey(deletionRequestedFileKey)
        .destinationBucket(bucket)
        .destinationKey(DELETION_SCHEDULED_FILE_KEY_PREFIX + deletionRequestedFileKey)
        .build();
    try {
      s3Client.copyObject(copyRequest);
    } catch (NoSuchKeyException e) {
      log.warn(e.getMessage());
      // ignore
      // throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
    }

    deleteFilePermanently(bucket, deletionRequestedFileKey);
  }

  private void deleteFilePermanently(String bucket, String fileKey) {
    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
        .bucket(bucket)
        .key(fileKey)
        .build();
    try {
      s3Client.deleteObject(deleteRequest);
    } catch (NoSuchKeyException e) {
      log.warn(e.getMessage());
      // ignore
      // throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
    }
  }

  private String extractFileName(String fileKey) {
    int idx = fileKey.lastIndexOf('/');
    return (idx >= 0) ? fileKey.substring(idx + 1) : fileKey;
  }

  // --- ContentType 유효성 검사 메서드 ---
  public static void validateImageContentType(String contentType) {
    if (!contentType.startsWith("image/")) {
      throw new BusinessException(FileErrorCode.UNSUPPORTED_FILE_TYPE);
    }
  }

  public static void validateVideoContentType(String contentType) {
    if (!contentType.startsWith("video/")) {
      throw new BusinessException(FileErrorCode.UNSUPPORTED_FILE_TYPE);
    }
  }
}