package kr.muroom.muroombackendbach.inquiry.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryStatus;
import lombok.Builder;

@Builder
public record InquiryResponse(
    Long id,
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

  }

  @Builder
  public record ImageDto(
      Long id,
      String imageKey
  ) {

  }

  @Builder
  public record Reply(
      String content,
      List<String> fileKeys
  ) {

  }
}
