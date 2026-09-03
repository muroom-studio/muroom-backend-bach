package kr.muroom.muroombackendbach.studio.application.query;

import java.util.List;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioDraft;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioDraftRepository;
import kr.muroom.muroombackendbach.studio.exception.StudioErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudioDraftQueryService {

  private final StudioDraftRepository studioDraftRepository;

  public List<StudioDraft> getOwnerStudioDrafts(Long ownerId) {
    return studioDraftRepository.findAllByOwnerIdOrderByUpdatedAtDesc(ownerId);
  }

  public StudioDraft getStudioDraft(Long ownerId, Long draftId) {
    return studioDraftRepository.findByIdAndOwnerId(draftId, ownerId)
        .orElseThrow(() -> new BusinessException(StudioErrorCode.DRAFT_NOT_FOUND));
  }
}
