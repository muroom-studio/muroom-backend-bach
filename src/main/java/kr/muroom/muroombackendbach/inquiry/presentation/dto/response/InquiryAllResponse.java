package kr.muroom.muroombackendbach.inquiry.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryCategory;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryStatus;
import lombok.Builder;

@Builder
public record InquiryAllResponse(
    String id,
    String title,
    String content,
    InquiryStatus status,
    CategoryDto category,
    List<ImageDto> images,
    Reply reply,
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

  @Builder
  public record Reply(
      String id,
      @Schema(example = "안녕하세요, 관리자입니다. 해결 완료")
      String content,
      List<ImageDto> imageUrls
  ) {

  }

}
