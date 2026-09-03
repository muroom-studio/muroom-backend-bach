package kr.muroom.muroombackendbach.studio.presentation.dto.response;

import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioDraft;
import lombok.Builder;

@Builder
public record StudioDraftListResponse(
    String studioDraftId,
    String studioName,
    Integer step,
    OffsetDateTime updatedAt
) {

  public static StudioDraftListResponse from(StudioDraft studioDraft) {
    return StudioDraftListResponse.builder()
        .studioDraftId(String.valueOf(studioDraft.getId()))
        .studioName(studioDraft.getStudioDraftData().studioName())
        .step(studioDraft.getStep())
        .updatedAt(studioDraft.getUpdatedAt())
        .build();
  }
}
