package kr.muroom.muroombackendbach.inquiry.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import kr.muroom.muroombackendbach.inquiry.domain.entity.Inquiry;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryImage;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryStatus;
import lombok.Builder;

@Builder
public record SearchInquiryResponse(
    Long id,
    String title,
    String content,
    InquiryStatus status,
    CategoryDto category,
    List<ImageDto> images,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

  public static SearchInquiryResponse from(Inquiry inquiry) {
    return SearchInquiryResponse.builder()
        .id(inquiry.getId())
        .title(inquiry.getTitle())
        .content(inquiry.getContent())
        .status(inquiry.getStatus())
        .category(CategoryDto.from(inquiry.getCategory()))
        .images(ImageDto.from(inquiry.getImages()))
        .createdAt(inquiry.getCreatedAt())
        .updatedAt(inquiry.getUpdatedAt())
        .build();
  }

  @Builder
  public record CategoryDto(
      String code,
      String name
  ) {

    public static CategoryDto from(
        kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryCategory category) {
      return CategoryDto.builder()
          .code(category.getCode())
          .name(category.getName())
          .build();
    }
  }

  @Builder
  public record ImageDto(
      Long id,
      String imageKey
  ) {

    public static ImageDto from(InquiryImage image) {
      return ImageDto.builder()
          .id(image.getId())
          .imageKey(image.getImageKey())
          .build();
    }

    public static List<ImageDto> from(List<InquiryImage> images) {
      return images.stream()
          .map(ImageDto::from)
          .toList();
    }
  }
}
