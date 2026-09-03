package kr.muroom.muroombackendbach.studio.presentation.dto.response;

import kr.muroom.muroombackendbach.studio.domain.entity.StudioDraft;
import kr.muroom.muroombackendbach.studio.domain.valueobject.StudioDraftData;
import lombok.Builder;

@Builder
public record StudioDraftDetailResponse(
    String studioDraftId,
    Integer step,
    StudioDraftData studioDraftData
) {

  public static StudioDraftDetailResponse from(StudioDraft studioDraft) {
    return StudioDraftDetailResponse.builder()
        .studioDraftId(String.valueOf(studioDraft.getId()))
        .step(studioDraft.getStep())
        .studioDraftData(studioDraft.getStudioDraftData())
        .build();
  }
}
