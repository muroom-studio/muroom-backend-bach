package kr.muroom.muroombackendbach.filestorage.application;

import java.time.Duration;
import java.util.UUID;
import kr.muroom.muroombackendbach.filestorage.domain.BucketType;
import kr.muroom.muroombackendbach.filestorage.domain.FileStorageLocation;
import kr.muroom.muroombackendbach.filestorage.infrastructure.S3Executor;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.request.FileUploadRequest;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FileStorageService {

  private final S3Executor s3Executor;
  private final String privateBucket;
  private final String publicBucket;
  private final Duration expiration;

  public FileStorageService(
      S3Executor s3Executor,
      @Value("${cloud.aws.s3.private-bucket-name}") String privateBucket,
      @Value("${cloud.aws.s3.public-bucket-name}") String publicBucket,
      @Value("${cloud.aws.s3.presign.expiration}") Duration expiration
  ) {
    this.s3Executor = s3Executor;
    this.privateBucket = privateBucket;
    this.publicBucket = publicBucket;
    this.expiration = expiration;
  }

  public GeneratePresignedPutUrlResponse getUploadUrl(FileStorageLocation sourceLocation, FileUploadRequest request) {
    String bucket = resolveBucket(sourceLocation.getBucketType());
    String key = String.format("%s%s/%s-%s", sourceLocation.getPrefix(), request.getDomainDirectory(), UUID.randomUUID(),
        request.getFileName());
    String url = s3Executor.presignUploadUrl(bucket, key, request.getContentType(), expiration);
    return new GeneratePresignedPutUrlResponse(url, key);
  }

  public String getViewUrl(String key, FileStorageLocation sourceLocation) {
    if (key == null || key.isBlank()) {
      return null;
    }
    String bucket = resolveBucket(sourceLocation.getBucketType());
    return switch (sourceLocation.getBucketType()) {
      case PUBLIC -> s3Executor.getPublicUrl(bucket, key);
      case PRIVATE -> s3Executor.presignViewUrl(bucket, key, expiration);
    };
  }

  public String move(String key, FileStorageLocation sourceLocation, FileStorageLocation destinationLocation) {
    String sourceBucket = resolveBucket(sourceLocation.getBucketType());
    String destinationBucket = resolveBucket(destinationLocation.getBucketType());
    String destinationKey = destinationLocation.generateFullKey(sourceLocation.extractPureFileName(key));

    s3Executor.copy(sourceBucket, key, destinationBucket, destinationKey);
    s3Executor.hardDelete(sourceBucket, key);
    return destinationKey;
  }

  public void softDelete(String key, FileStorageLocation sourceLocation) {
    move(key, sourceLocation, sourceLocation.getTrashLocation());
  }

  public String copyToReportSnapshot(String key, FileStorageLocation sourceLocation, String domain) {
    // from here
    String sourceBucket = resolveBucket(sourceLocation.getBucketType());
    String destinationBucket = resolveBucket(FileStorageLocation.PRIVATE_REPORT.getBucketType());

    String fileName = sourceLocation.extractPureFileName(key);
    String snapshotKey = FileStorageLocation.PRIVATE_REPORT.generateFullKey(
        domain.toLowerCase() + "/" + UUID.randomUUID() + "-" + fileName);

    s3Executor.copy(sourceBucket, key, destinationBucket, snapshotKey);
    return snapshotKey;
  }

  private String resolveBucket(BucketType bucketType) {
    return switch (bucketType) {
      case PUBLIC -> publicBucket;
      case PRIVATE -> privateBucket;
    };
  }
}
