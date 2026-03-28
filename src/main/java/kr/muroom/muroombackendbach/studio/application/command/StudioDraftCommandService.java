package kr.muroom.muroombackendbach.studio.application.command;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.domain.FileStorageLocation;
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
  private final FileStorageService fileStorageService;

  public Long createStudioDraft(CreateStudioDraftCommand command) {
    StudioDraft newStudioDraft = StudioDraft.builder()
        .ownerId(command.ownerId())
        .step(command.step())
        .studioName(command.studioDraftData().studioName())
        .studioDraftData(command.studioDraftData())
        .expiresAt(OffsetDateTime.now().plusDays(3))
        .build();

    return studioDraftRepository.save(newStudioDraft).getId();
  }

  public void updateStudioDraft(UpdateStudioDraftCommand command) {
    StudioDraft studioDraft = studioDraftRepository.findByIdAndOwnerId(command.studioDraftId(), command.ownerId())
        .orElseThrow(() -> new BusinessException(StudioErrorCode.DRAFT_NOT_FOUND));

    List<String> oldKeys = studioDraft.getStudioDraftData().extractAllImageKeys();
    Set<String> newKeys = new HashSet<>(command.studioDraftData().extractAllImageKeys());

    oldKeys.stream()
        .filter(key -> !newKeys.contains(key))
        .forEach(key -> fileStorageService.softDelete(key, FileStorageLocation.PRIVATE_DRAFT));

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

    studioDraft.getStudioDraftData().extractAllImageKeys()
        .forEach(key -> fileStorageService.softDelete(key, FileStorageLocation.PRIVATE_DRAFT));

    studioDraftRepository.delete(studioDraft);
  }

  public void deleteExpiredDrafts() {
    List<StudioDraft> expiredDrafts = studioDraftRepository.findAllByExpiresAtBefore(OffsetDateTime.now());

    expiredDrafts.forEach(draft ->
        draft.getStudioDraftData().extractAllImageKeys()
            .forEach(key -> fileStorageService.softDelete(key, FileStorageLocation.PRIVATE_DRAFT))
    );

    studioDraftRepository.deleteAll(expiredDrafts);
  }
}
