package kr.muroom.muroombackendbach.admin.inquiry.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record InquiryReplyImagePresignedUrlRequest(
    @NotNull
    List<InquiryReplyImageInfo> inquiryReplyImages
) {

  public record InquiryReplyImageInfo(
      @NotBlank String fileName,
      @NotNull Long inquiryId,
      @NotBlank String contentType
  ) {

  }
}