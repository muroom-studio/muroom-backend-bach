package kr.muroom.muroombackendbach.inquiry.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.request.FileUploadRequest;

public record InquiryImageUploadRequest(
    @Schema(description = "파일 이름", example = "inquiry_image_01.jpg")
    @NotBlank String fileName,

    @Schema(description = "콘텐츠 타입", example = "image/jpeg")
    @NotBlank String contentType
) implements FileUploadRequest {

  private static final String DOMAIN_DIRECTORY_PREFIX = "inquiries/requests";

  @Override
  public String getFileName() {
    return fileName;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public String getDomainDirectory() {
    return DOMAIN_DIRECTORY_PREFIX;
  }
}