package kr.muroom.muroombackendbach.studioboasting.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.request.FileUploadRequest;
import net.minidev.json.annotate.JsonIgnore;

@Schema(description = "내 작업실 소개(자랑) 이미지 업로드 요청 DTO")
public record StudioBoastImageUploadRequest(
    @Schema(description = "파일 이름", example = "boast-image.png")
    String fileName,

    @Schema(description = "콘텐츠 타입", example = "image/png")
    String contentType
) implements FileUploadRequest {

  private static final String DOMAIN_DIRECTORY = "studio-boasts";

  @Override
  public String getFileName() {
    return fileName;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  @JsonIgnore
  public String getDomainDirectory() {
    return DOMAIN_DIRECTORY;
  }
}
