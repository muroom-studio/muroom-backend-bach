package kr.muroom.muroombackendbach.studio.application.command.dto;

import kr.muroom.muroombackendbach.studio.domain.valueobject.StudioDraftData;
import lombok.Builder;

@Builder
public record UpdateStudioDraftCommand(
    Long ownerId,
    Long studioDraftId,
    Integer step,
    StudioDraftData studioDraftData
) {
}
