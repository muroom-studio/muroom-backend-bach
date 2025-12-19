package kr.muroom.muroombackendbach.inquiry.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryStatus;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryResponse.CategoryDto;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryResponse.ImageDto;
import lombok.Builder;

public record SearchInquiryResponse(
    Long id,
    String title,
    String content,
    InquiryStatus status,
    InquiryResponse.CategoryDto category,
    List<InquiryResponse.ImageDto> images,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

  @Builder
  public record CategoryDto(
      String code,
      String name
  ) {

  }

  @Builder
  public record ImageDto(
      Long id,
      String imageKey
  ) {

  }
}
