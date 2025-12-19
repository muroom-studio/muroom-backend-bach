package kr.muroom.muroombackendbach.admin.inquiry.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record InquiryReplyImagePresignedUrlRequest(
    @NotNull
    List<InquiryReplyImageInfo> inquiryReplyImages
) {

  public record InquiryReplyImageInfo(
      @NotBlank String fileName,
      @Schema(example = "1", description = "답글을 남길 문의의 ID 값이 필요합니다.")
      @NotNull Long inquiryId,
      @Schema(example = "image/")
      @NotBlank String contentType
  ) {

  }
}