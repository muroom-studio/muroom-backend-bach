package kr.muroom.muroombackendbach.studio.application.command.dto;

import kr.muroom.muroombackendbach.studio.domain.valueobject.StudioDraftData;
import lombok.Builder;

@Builder
public record CreateStudioDraftCommand(
    Long ownerId,
    Integer step,
    StudioDraftData studioDraftData
) {
}
