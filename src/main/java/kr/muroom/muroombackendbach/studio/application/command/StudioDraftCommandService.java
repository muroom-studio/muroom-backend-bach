package kr.muroom.muroombackendbach.studio.application.command;

import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.studio.application.command.dto.CreateStudioDraftCommand;
import kr.muroom.muroombackendbach.studio.application.command.dto.UpdateStudioDraftCommand;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioDraft;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioDraftRepository;
import kr.muroom.muroombackendbach.studio.exception.StudioErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudioDraftCommandService {

  private final StudioDraftRepository studioDraftRepository;

  public Long createStudioDraft(CreateStudioDraftCommand command) {
    StudioDraft newStudioDraft = StudioDraft.builder()
        .ownerId(command.ownerId())
        .step(command.step())
        .studioDraftData(command.studioDraftData())
        .expiresAt(OffsetDateTime.now().plusDays(3))
        .build();

    return studioDraftRepository.save(newStudioDraft).getId();
  }

  public void updateStudioDraft(UpdateStudioDraftCommand command) {
    StudioDraft studioDraft = studioDraftRepository.findByIdAndOwnerId(command.studioDraftId(), command.ownerId())
        .orElseThrow(() -> new BusinessException(StudioErrorCode.DRAFT_NOT_FOUND));

    studioDraft.update(
        command.step(),
        command.studioDraftData().studioName(),
        command.studioDraftData(),
        OffsetDateTime.now().plusDays(3)
    );
  }

  public void deleteStudioDraft(Long ownerId, Long draftId) {
    StudioDraft studioDraft = studioDraftRepository.findByIdAndOwnerId(draftId, ownerId)
        .orElseThrow(() -> new BusinessException(StudioErrorCode.DRAFT_NOT_FOUND));

    studioDraftRepository.delete(studioDraft);
  }
}
