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

  public FileStorageService(
      S3Presigner s3Presigner,
      @Value("${cloud.aws.s3.bucket}") String bucket,
      @Value("${cloud.aws.s3.presign.expiration}") Duration expiration
  ) {
    this.s3Presigner = s3Presigner;
    this.bucket = bucket;
    this.expiration = expiration;
  }

  public record PresignedPutUrlDto(String url, String fileKey) {

  }

  public PresignedPutUrlDto generatePresignedPutUrl(String fileName, String domain,
      String contentType) {
    String fileKey = domain + "/" + UUID.randomUUID() + "-" + fileName;

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
