package kr.muroom.muroombackendbach.filestorage.presentation.dto.response;

import java.util.List;

public record GeneratePresignedPutUrlsResponse(
    List<PresignedUrlInfo> presignedUrls
) {

  public record PresignedUrlInfo(
      String url,
      String fileKey
  ) {

  }
}
