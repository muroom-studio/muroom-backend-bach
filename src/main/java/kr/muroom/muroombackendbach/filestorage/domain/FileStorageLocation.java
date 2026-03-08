package kr.muroom.muroombackendbach.filestorage.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum FileStorageLocation {

  PUBLIC_PERMANENT(BucketType.PUBLIC, ""),
  PUBLIC_TEMP(BucketType.PUBLIC, "temp/"),
  PUBLIC_TRASH(BucketType.PUBLIC, "deletion-scheduled/"),

  PRIVATE_PERMANENT(BucketType.PRIVATE, ""),
  PRIVATE_TEMP(BucketType.PRIVATE, "temp/"),
  PRIVATE_DRAFT(BucketType.PRIVATE, "draft/"),
  PRIVATE_REPORT(BucketType.PRIVATE, "snapshot/report/"),
  PRIVATE_TRASH(BucketType.PRIVATE, "deletion-scheduled/"),
  ;

  private final BucketType bucketType;
  private final String prefix;

  public FileStorageLocation getTrashLocation() {
    return switch (this.bucketType) {
      case PUBLIC -> PUBLIC_TRASH;
      case PRIVATE -> PRIVATE_TRASH;
    };
  }

  public String extractPureFileName(String fullKey) {
    return (fullKey != null && fullKey.startsWith(prefix)) ? fullKey.substring(prefix.length()) : fullKey;
  }

  public String generateFullKey(String purePath) {
    return this.prefix + purePath;
  }
}
