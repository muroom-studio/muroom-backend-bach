package kr.muroom.muroombackendbach.filestorage.application;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.exception.FileErrorCode;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.request.FileUploadRequest;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlsResponse;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlsResponse.PresignedUrlInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
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
  private final String bucket;
  private final Duration expiration;

  private static final String TEMPORARY_FILE_KEY_PREFIX = "temp/";

  /**
   * 파일 저장 서비스 생성자입니다.
   *
   * @param s3Presigner S3 프리사이너 - S3와 상호작용하여 사전 서명된 URL을 생성합니다.
   * @param bucket      S3 버킷 이름
   * @param expiration  사전 서명된 URL의 만료 시간
   */
  public FileStorageService(
      S3Client s3Client,
      S3Presigner s3Presigner,
      @Value("${cloud.aws.s3.bucket}") String bucket,
      @Value("${cloud.aws.s3.presign.expiration}") Duration expiration
  ) {
    this.s3Client = s3Client;
    this.s3Presigner = s3Presigner;
    this.bucket = bucket;
    this.expiration = expiration;
  }

  /**
   * 사전 서명된 업로드 URL과 파일 키를 포함하는 DTO입니다.
   *
   * @param url     사전 서명된 업로드 URL
   * @param fileKey S3에 저장될 파일 키
   */
  public record PresignedPutUrlDto(String url, String fileKey) {

  }

  /**
   * 여러 파일에 대한 사전 서명된 업로드 URL을 생성합니다.
   *
   * @param fileRequests         업로드할 파일들의 요청 정보 리스트
   * @param contentTypeValidator 파일의 콘텐츠 타입을 검증하는 함수형 인터페이스
   * @return 사전 서명된 업로드 URL과 파일 키를 포함하는 응답 DTO
   */
  public GeneratePresignedPutUrlsResponse generatePresignedPutUrls(
      List<? extends FileUploadRequest> fileRequests, Consumer<String> contentTypeValidator
  ) {
    List<PresignedUrlInfo> presignedUrlInfos = fileRequests.stream()
        .map(fileRequest -> {
          contentTypeValidator.accept(fileRequest.getContentType());
          PresignedPutUrlDto presignedUrl = generatePresignedPutUrl(
              fileRequest.getFileName(),
              fileRequest.getDomainDirectory(),
              fileRequest.getContentType()
          );
          return new PresignedUrlInfo(presignedUrl.url(), presignedUrl.fileKey());
        })
        .toList();

    return new GeneratePresignedPutUrlsResponse(presignedUrlInfos);
  }

  /**
   * 주어진 파일 이름과 도메인에 대해 사전 서명된 업로드 URL을 생성합니다.
   *
   * @param fileName    업로드할 파일의 이름
   * @param domain      파일이 속한 도메인 (예: "profile-images", "documents" 등)
   * @param contentType 파일의 MIME 타입 (예: "image/png", "application/pdf" 등)
   * @return 사전 서명된 업로드 URL과 파일 키를 포함하는 DTO
   */
  public PresignedPutUrlDto generatePresignedPutUrl(String fileName, String domain,
      String contentType) {
    String sanitizedFileName = fileName.replaceAll("[^a-zA-Z0-9.\\-_]", "_");
    String fileKey = "temp/" + domain + "/" + UUID.randomUUID() + "-" + sanitizedFileName;

    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(fileKey)
        .contentType(contentType)
        .build();

    PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
        .signatureDuration(expiration)
        .putObjectRequest(putObjectRequest)
        .build();

    PresignedPutObjectRequest presignedPutObjectRequest =
        s3Presigner.presignPutObject(presignRequest);

    return new PresignedPutUrlDto(presignedPutObjectRequest.url().toString(), fileKey);
  }

  public String moveFromTempToPermanent(String tempFileKey) {
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
    s3Client.copyObject(copyRequest);

    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
        .bucket(bucket)
        .key(tempFileKey)
        .build();
    try {
      s3Client.deleteObject(deleteRequest);
    } catch (NoSuchKeyException e) {
      log.warn(e.getMessage());
      throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
    }

    return permanentFileKey;
  }

  public void deleteFile(String fileKey) {
    if (fileKey == null || fileKey.isBlank()) {
      throw new BusinessException(FileErrorCode.INVALID_TEMP_FILE_KEY);
    }
    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
        .bucket(bucket)
        .key(fileKey)
        .build();
    try {
      s3Client.deleteObject(deleteRequest);
    } catch (NoSuchKeyException e) {
      log.warn(e.getMessage());
      throw new BusinessException(FileErrorCode.FILE_NOT_FOUND);
    }

  }

  /**
   * 주어진 파일 키에 대해 사전 서명된 조회 및 다운로드 URL을 생성합니다.
   *
   * @param fileKey S3에 저장된 파일의 키
   * @return 사전 서명된 조회 및 다운로드 URL
   */
  public String generatePresignedGetUrl(String fileKey) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(fileKey)
        .build();

    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(expiration)
        .getObjectRequest(getObjectRequest)
        .build();

    PresignedGetObjectRequest presignedGetObjectRequest =
        s3Presigner.presignGetObject(presignRequest);

    return presignedGetObjectRequest.url().toString();
  }

  public static void validateImageContentType(String contentType) {
    if (!contentType.startsWith("image/")) {
      throw new BusinessException(FileErrorCode.UNSUPPORTED_FILE_TYPE);
    }
  }
}
