package kr.muroom.muroombackendbach.inquiry.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record InquiryImagePresignedUrlRequest(
    @NotNull
    List<InquiryImageInfo> inquiryImages
) {

  public record InquiryImageInfo(
      @NotBlank String fileName,

      @Schema(example = "1", description = "문의 카테고리 ID 값이 필요합니다.")
      @NotNull Long categoryId,

      @Schema(example = "image/~")
      @NotBlank String contentType
  ) {

  }
}