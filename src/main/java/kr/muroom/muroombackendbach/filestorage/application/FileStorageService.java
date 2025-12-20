package kr.muroom.muroombackendbach.filestorage.application;

import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
public class FileStorageService {

  private final S3Presigner s3Presigner;
  private final String bucket;
  private final Duration expiration;

  /**
   * 파일 저장 서비스 생성자입니다.
   *
   * @param s3Presigner S3 프리사이너 - S3와 상호작용하여 사전 서명된 URL을 생성합니다.
   * @param bucket      S3 버킷 이름
   * @param expiration  사전 서명된 URL의 만료 시간
   */
  public FileStorageService(
      S3Presigner s3Presigner,
      @Value("${cloud.aws.s3.bucket}") String bucket,
      @Value("${cloud.aws.s3.presign.expiration}") Duration expiration
  ) {
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
    String fileKey = domain + "/" + UUID.randomUUID() + "-" + sanitizedFileName;

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
}
