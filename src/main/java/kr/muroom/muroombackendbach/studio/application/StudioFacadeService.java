package kr.muroom.muroombackendbach.studio.application;

import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudioFacadeService {

  private final StudioOptionService studioOptionService;
  private final StudioRepository studioRepository;
  private final StudioService studioService;

  public void getStudioDetail(Long studioId) {
    // 1. 작업실 조회
    Studio studio = studioService.getStudio(studioId);

  }

}
