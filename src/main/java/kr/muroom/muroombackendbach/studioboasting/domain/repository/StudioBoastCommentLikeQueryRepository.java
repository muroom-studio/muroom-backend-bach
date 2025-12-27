package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastComment;

public interface StudioBoastCommentLikeQueryRepository {

  Set<Long> findLikedCommentIdsByCreatorUserIdAndCommentIn(Long requestUserId, List<StudioBoastComment> commentsOnPage);

  Map<Long, Long> findLikeCountsByCommentIn(List<StudioBoastComment> comments);

  void deleteAllByStudioBoast(StudioBoast studioBoast);
}
