package kr.muroom.muroombackendbach.inquiry.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record InquiryImagePresignedUrlRequest(
    @NotNull
    List<InquiryImageInfo> inquiryImages
) {

  public record InquiryImageInfo(
      @NotBlank String fileName,
      @NotBlank Long categoryId,
      @NotBlank String contentType
  ) {

  }
}