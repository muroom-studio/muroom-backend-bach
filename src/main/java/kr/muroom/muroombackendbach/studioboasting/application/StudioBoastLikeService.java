package kr.muroom.muroombackendbach.studioboasting.application;

import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastLike;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastLikeRepository;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastRepository;
import kr.muroom.muroombackendbach.studioboasting.exception.StudioBoastErrorCode;
import kr.muroom.muroombackendbach.user.application.MusicianService;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudioBoastLikeService {

  private final StudioBoastRepository studioBoastRepository;
  private final StudioBoastLikeRepository studioBoastLikeRepository;
  private final MusicianService musicianService;

  public void likeStudioBoast(Long studioBoastId, Long musicianId) {
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));
    Musician musician = musicianService.getMusicianById(musicianId);

    if (studioBoastLikeRepository.existsByMusicianAndStudioBoast(musician, studioBoast)) {
      return; // 이미 좋아요를 누른 경우
    }

    StudioBoastLike newLike = StudioBoastLike.builder()
        .musician(musician)
        .studioBoast(studioBoast)
        .build();
    studioBoastLikeRepository.save(newLike);

    studioBoast.adjustLikeCount(1);
  }
  
  public void unlikeStudioBoast(Long studioBoastId, Long musicianId) {
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));
    Musician musician = musicianService.getMusicianById(musicianId);

    studioBoastLikeRepository.findByMusicianAndStudioBoast(musician, studioBoast)
        .ifPresent(like -> {
          studioBoastLikeRepository.delete(like);
          studioBoast.adjustLikeCount(-1);
        });
  }
}
