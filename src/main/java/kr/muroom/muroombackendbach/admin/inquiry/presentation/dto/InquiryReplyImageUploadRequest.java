package kr.muroom.muroombackendbach.admin.inquiry.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.request.FileUploadRequest;

public record InquiryReplyImageUploadRequest(
    @Schema(example = "reply_image_01.png", description = "업로드할 파일의 이름이 필요합니다.")
    @NotBlank String fileName,

    @Schema(example = "78962138792321", description = "답글을 남길 문의의 ID 값이 필요합니다.")
    @NotNull Long inquiryId,

    @Schema(example = "image/")
    @NotBlank String contentType
) implements FileUploadRequest {

  private static final String DOMAIN_DIRECTORY_PREFIX = "inquiries/responses";

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