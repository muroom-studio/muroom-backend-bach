package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import java.util.List;
import java.util.Map;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;

public interface StudioBoastCommentQueryRepository {
  
  Map<Long, Long> countTopLevelCommentsByStudioBoastIn(List<StudioBoast> studioBoasts);

  Long countTopLevelCommentsByStudioBoast(StudioBoast studioBoast);

}
