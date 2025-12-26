package kr.muroom.muroombackendbach.studioboasting.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.auth.exception.AuthErrorCode;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastComment;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastCommentLike;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastCommentLikeRepository;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastCommentRepository;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastRepository;
import kr.muroom.muroombackendbach.studioboasting.exception.StudioBoastErrorCode;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.request.CreateStudioBoastCommentRequest;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastCommentReplyResponse;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastCommentResponse;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastCommentResponse.CreatorUserInfo;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastCommentResponse.TaggedUserInfo;
import kr.muroom.muroombackendbach.user.application.MusicianService;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudioBoastCommentService {

  private final StudioBoastCommentRepository studioBoastCommentRepository;
  private final StudioBoastRepository studioBoastRepository;
  private final StudioBoastCommentLikeRepository studioBoastCommentLikeRepository;
  private final MusicianService musicianService;

  @Transactional
  public Long createComment(Long studioBoastId, Long musicianId, CreateStudioBoastCommentRequest request) {
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));

    StudioBoastComment parentComment = null;
    if (request.parentId() != null) {
      parentComment = studioBoastCommentRepository.findById(request.parentId())
          .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_COMMENT_NOT_FOUND));

      if (parentComment.getParent() != null) {
        throw new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_COMMENT_CANNOT_REPLY_TO_REPLY);
      }
    }

    StudioBoastComment newComment = StudioBoastComment.builder()
        .content(request.content())
        .isSecret(request.isSecret())
        .creatorUserId(musicianId)
        .studioBoast(studioBoast)
        .parent(parentComment)
        .taggedUserId(request.taggedUserId())
        .build();

    studioBoastCommentRepository.save(newComment);
    return newComment.getId();
  }

  @Transactional
  public void updateComment(Long commentId, String content, Long musicianId) {
    StudioBoastComment comment = studioBoastCommentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_COMMENT_NOT_FOUND));

    if (!comment.getCreatorUserId().equals(musicianId)) {
      throw new BusinessException(AuthErrorCode.FORBIDDEN);
    }
    comment.updateContent(content);
  }

  @Transactional
  public void deleteComment(Long commentId, Long musicianId) {
    StudioBoastComment comment = studioBoastCommentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_COMMENT_NOT_FOUND));

    if (!comment.getCreatorUserId().equals(musicianId)) {
      throw new BusinessException(AuthErrorCode.FORBIDDEN);
    }
    studioBoastCommentRepository.delete(comment);
  }

  public Page<StudioBoastCommentResponse> getComments(Long studioBoastId, Long requestUserId, Pageable pageable) {
    StudioBoast studioBoast = studioBoastRepository.findById(studioBoastId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_NOT_FOUND));
    Long postAuthorId = studioBoast.getCreatorUserId();

    // 1. 최상위 댓글 페이징 조회
    Page<StudioBoastComment> rootCommentPage = studioBoastCommentRepository.findByStudioBoastAndParentIsNull(studioBoast, pageable);
    List<StudioBoastComment> rootComments = rootCommentPage.getContent();
    if (rootComments.isEmpty()) {
      return Page.empty(pageable);
    }

    // 2. 최상위 댓글에 대한 모든 대댓글 조회
    List<StudioBoastComment> commentsOnPage = new ArrayList<>(rootComments);
    List<StudioBoastComment> allReplies = studioBoastCommentRepository.findByParentInOrderByCreatedAtAsc(rootComments);
    commentsOnPage.addAll(allReplies);

    // 3. 요청 사용자가 좋아요 누른 댓글 ID 일괄 조회
    final Set<Long> likedCommentIds;
    if (requestUserId != null) {
      likedCommentIds = studioBoastCommentLikeRepository.findLikedCommentIdsByCreatorUserIdAndCommentIn(requestUserId, commentsOnPage);
    } else {
      likedCommentIds = Collections.emptySet();
    }

    Map<Long, Long> likeCounts = studioBoastCommentLikeRepository.findLikeCountsByCommentIn(commentsOnPage);

    // 4. 댓글 작성자 및 태그된 사용자 정보 일괄 조회
    Set<Long> allMusicianIds = commentsOnPage.stream()
        .map(StudioBoastComment::getCreatorUserId)
        .collect(Collectors.toSet());
    commentsOnPage.stream()
        .filter(comment -> comment.getTaggedUserId() != null)
        .map(StudioBoastComment::getTaggedUserId)
        .forEach(allMusicianIds::add);
    Map<Long, Musician> musiciansById = musicianService.getMusiciansAsMapByIds(new ArrayList<>(allMusicianIds));

    // 5. 댓글 및 대댓글 DTO 변환 및 계층 구조 빌드
    Map<Long, List<StudioBoastComment>> repliesByParentId = allReplies.stream()
        .collect(Collectors.groupingBy(comment -> comment.getParent().getId()));

    List<StudioBoastCommentResponse> commentResponses = rootComments.stream()
        .map(rootComment -> {
          List<StudioBoastComment> directReplies = repliesByParentId.getOrDefault(rootComment.getId(), Collections.emptyList());

          List<StudioBoastCommentReplyResponse> replyResponses = directReplies.stream()
              .map(reply -> buildReplyResponse(
                  reply, postAuthorId, requestUserId, musiciansById, likedCommentIds, likeCounts
              ))
              .toList();

          return buildCommentResponse(rootComment, postAuthorId, requestUserId, musiciansById, likedCommentIds, likeCounts, replyResponses);
        })
        .toList();

    return new PageImpl<>(commentResponses, pageable, rootCommentPage.getTotalElements());
  }

  private StudioBoastCommentResponse buildCommentResponse(
      StudioBoastComment comment, Long postAuthorId, Long requestUserId, Map<Long, Musician> musiciansById,
      Set<Long> likedCommentIds, Map<Long, Long> likeCounts, List<StudioBoastCommentReplyResponse> replies
  ) {
    // 삭제된 댓글 처리
    if (comment.getDeletedAt() != null) {
      return StudioBoastCommentResponse.createDeletedPlaceholder(comment.getId(), replies);
    }
    // 비밀 댓글 처리 (권한 확인)
    if (!canViewComment(comment, postAuthorId, requestUserId)) {
      return StudioBoastCommentResponse.createSecretPlaceholder(comment.getId(), replies);
    }

    // DTO에 필요한 데이터 조회 및 계산
    Musician creator = musiciansById.get(comment.getCreatorUserId());
    Musician taggedUser = (comment.getTaggedUserId() != null) ? musiciansById.get(comment.getTaggedUserId()) : null;
    Boolean isWritten = (requestUserId != null) ? requestUserId.equals(comment.getCreatorUserId()) : null;
    Boolean isLiked = (requestUserId != null) ? likedCommentIds.contains(comment.getId()) : null;
    Long likeCount = likeCounts.getOrDefault(comment.getId(), 0L);

    // 최종 DTO 빌드
    return StudioBoastCommentResponse.builder()
        .id(comment.getId().toString())
        .content(comment.getContent())
        .createdAt(comment.getCreatedAt())
        .isSecret(comment.getIsSecret())
        .isDeleted(false)
        .creatorUserInfo(CreatorUserInfo.from(creator))
        .taggedUserInfo(TaggedUserInfo.from(taggedUser))
        .replies(replies)
        .likeCount(likeCount)
        .isWrittenByRequestUser(isWritten)
        .isLikedByRequestUser(isLiked)
        .build();
  }

  private boolean canViewComment(StudioBoastComment comment, Long postAuthorId, Long requestUserId) {
    if (!comment.getIsSecret()) {
      return true;
    }
    if (requestUserId == null) {
      return false;
    }
    return requestUserId.equals(postAuthorId)
        || requestUserId.equals(comment.getCreatorUserId())
        || (comment.getTaggedUserId() != null && requestUserId.equals(comment.getTaggedUserId()));
  }

  private StudioBoastCommentReplyResponse buildReplyResponse(
      StudioBoastComment reply, Long postAuthorId, Long requestUserId,
      Map<Long, Musician> musiciansById, Set<Long> likedCommentIds, Map<Long, Long> likeCounts
  ) {
    if (reply.getDeletedAt() != null) {
      return StudioBoastCommentReplyResponse.createDeletedPlaceholder(reply.getId());
    }
    if (!canViewComment(reply, postAuthorId, requestUserId)) {
      return StudioBoastCommentReplyResponse.createSecretPlaceholder(reply.getId());
    }

    Musician creator = musiciansById.get(reply.getCreatorUserId());
    Musician taggedUser = (reply.getTaggedUserId() != null) ? musiciansById.get(reply.getTaggedUserId()) : null;
    Boolean isWritten = (requestUserId != null) ? requestUserId.equals(reply.getCreatorUserId()) : null;
    Boolean isLiked = (requestUserId != null) ? likedCommentIds.contains(reply.getId()) : null;
    Long likeCount = likeCounts.getOrDefault(reply.getId(), 0L);

    return StudioBoastCommentReplyResponse.builder()
        .id(reply.getId().toString())
        .content(reply.getContent())
        .createdAt(reply.getCreatedAt())
        .isSecret(reply.getIsSecret())
        .isDeleted(false)
        .creatorUserInfo(CreatorUserInfo.from(creator))
        .taggedUserInfo(TaggedUserInfo.from(taggedUser))
        .likeCount(likeCount)
        .isWrittenByRequestUser(isWritten)
        .isLikedByRequestUser(isLiked)
        .build();
  }

  // ===== ===== ===== ===== 댓글 좋아요 ===== ===== ===== =====
  @Transactional
  public void likeComment(Long commentId, Long musicianId) {
    StudioBoastComment comment = studioBoastCommentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_COMMENT_NOT_FOUND));

    if (studioBoastCommentLikeRepository.findByMusicianIdAndComment(musicianId, comment).isPresent()) {
      return;
    }

    StudioBoastCommentLike newCommentLike = StudioBoastCommentLike.builder()
        .musicianId(musicianId)
        .comment(comment)
        .build();
    studioBoastCommentLikeRepository.save(newCommentLike);
  }

  @Transactional
  public void unlikeComment(Long commentId, Long musicianId) {
    StudioBoastComment comment = studioBoastCommentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_COMMENT_NOT_FOUND));

    studioBoastCommentLikeRepository.findByMusicianIdAndComment(musicianId, comment)
        .ifPresent(studioBoastCommentLikeRepository::delete);
  }
}
