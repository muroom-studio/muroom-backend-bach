package kr.muroom.muroombackendbach.filestorage.infrastructure;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@RequiredArgsConstructor
public class S3Executor {

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  /**
   * S3에서 객체를 복사하는 기능입니다. 동일한 버킷 내에서 객체의 키를 변경하는 경우에 이 메서드를 사용할 수 있습니다.
   */
  public void copy(String sourceBucket, String sourceKey, String destinationKey) {
    copy(sourceBucket, sourceKey, sourceBucket, destinationKey);
  }

  /**
   * S3에서 객체를 복사하는 기능입니다. S3는 객체를 이동하는 기능이 없으므로, 복사 후 원본을 삭제하는 방식으로 "이동"을 구현할 수 있습니다.
   */
  public void copy(String sourceBucket, String sourceKey, String destinationBucket, String destinationKey) {
    CopyObjectRequest copyRequest = CopyObjectRequest.builder()
        .sourceBucket(sourceBucket)
        .sourceKey(sourceKey)
        .destinationBucket(destinationBucket)
        .destinationKey(destinationKey)
        .build();
    s3Client.copyObject(copyRequest);
  }

  /**
   * S3에서 객체를 영구적으로 즉시 삭제합니다.
   *
   * <p>비즈니스 레벨의 삭제가 필요한 경우 이 메서드 대신 FileStorageService.softDelete()를 호출하십시오.
   */
  public void hardDelete(String bucket, String key) {
    DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();
    s3Client.deleteObject(deleteRequest);
  }

  /**
   * S3에 객체를 업로드하기 위한 presigned URL을 생성합니다.
   */
  public String presignUploadUrl(String bucket, String key, String contentType, Duration expiration) {
    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(contentType)
        .build();
    PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
        .signatureDuration(expiration)
        .putObjectRequest(putObjectRequest)
        .build();
    return s3Presigner.presignPutObject(presignRequest).url().toString();
  }

  /**
   * S3에서 객체를 다운로드하기 위한 presigned URL을 생성합니다.
   */
  public String presignViewUrl(String bucket, String key, Duration expiration) {
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();
    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
        .signatureDuration(expiration)
        .getObjectRequest(getObjectRequest)
        .build();
    return s3Presigner.presignGetObject(presignRequest).url().toString();
  }

  /**
   * S3 객체의 공개 URL을 반환합니다. 이 URL은 객체가 퍼블릭 액세스 권한을 가지고 있을 때만 유효합니다.
   */
  public String getPublicUrl(String bucket, String key) {
    GetUrlRequest getUrlRequest = GetUrlRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();
    return s3Client.utilities().getUrl(getUrlRequest).toString();
  }
}
