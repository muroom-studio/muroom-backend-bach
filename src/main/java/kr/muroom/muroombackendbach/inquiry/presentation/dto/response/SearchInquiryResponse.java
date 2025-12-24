package kr.muroom.muroombackendbach.inquiry.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryCategory;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryStatus;
import lombok.Builder;

@Builder
public record SearchInquiryResponse(
    String id,
    String title,
    String content,
    InquiryStatus status,
    CategoryDto category,
    List<ImageDto> images,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

  @Builder
  public record CategoryDto(
      String code,
      String name
  ) {

    public static CategoryDto from(InquiryCategory category) {
      return CategoryDto.builder()
          .code(category.getCode())
          .name(category.getName())
          .build();
    }
  }

}
