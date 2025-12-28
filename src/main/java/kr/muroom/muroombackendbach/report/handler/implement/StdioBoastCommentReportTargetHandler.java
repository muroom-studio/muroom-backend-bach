package kr.muroom.muroombackendbach.report.handler.implement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.report.domain.enums.ReportDomainType;
import kr.muroom.muroombackendbach.report.exception.ReportErrorCode;
import kr.muroom.muroombackendbach.report.handler.ReportTargetHandler;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoastComment;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastCommentRepository;
import kr.muroom.muroombackendbach.studioboasting.exception.StudioBoastErrorCode;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.user.domain.repository.MusicianRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StdioBoastCommentReportTargetHandler implements ReportTargetHandler {

  private final StudioBoastCommentRepository studioBoastCommentRepository;
  private final MusicianRepository musicianRepository;
  private final ObjectMapper objectMapper;

  @Override
  public ReportDomainType supports() {
    return ReportDomainType.STUDIO_BOAST_COMMENT;
  }

  @Override
  public void validateTarget(Long studioBoastCommentId, Musician reporter) {
    StudioBoastComment studioBoastComment = studioBoastCommentRepository.findById(
            studioBoastCommentId)
        .orElseThrow(
            () -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_COMMENT_NOT_FOUND));

    if (studioBoastComment.getCreatorUserId() != null
        && studioBoastComment.getCreatorUserId().equals(reporter.getId())) {
      throw new BusinessException(ReportErrorCode.REPORT_NOT_ME);
    }
  }

  @Override
  public JsonNode buildSnapshot(Long studioBoastCommentId) {
    StudioBoastComment c = studioBoastCommentRepository.findById(studioBoastCommentId)
        .orElseThrow(
            () -> new BusinessException(StudioBoastErrorCode.STUDIO_BOAST_COMMENT_NOT_FOUND));

    ObjectNode root = objectMapper.createObjectNode();

    root.put("id", c.getId());
    root.put("content", c.getContent());
    root.put("createdAt", c.getCreatedAt().toString());
    root.put("isSecret", Boolean.TRUE.equals(c.getIsSecret()));
    root.set("createdUserInfo", buildUserInfo(c.getCreatorUserId()));
    root.set("taggedUserInfo", buildUserInfo(c.getTaggedUserId()));

    return root;
  }

  private ObjectNode buildUserInfo(Long musicianId) {
    ObjectNode node = objectMapper.createObjectNode();
    if (musicianId == null) {
      node.putNull("id");
      node.putNull("nickname");
      return node;
    }

    Optional<Musician> musicianOpt = musicianRepository.findById(musicianId);
    if (musicianOpt.isEmpty()) {
      // 유저가 삭제/비활성/없는 경우 snapshot에는 최소한 id만 남기는 게 실무적으로 안전
      node.put("id", musicianId);
      node.putNull("nickname");
      return node;
    }

    Musician m = musicianOpt.get();
    node.put("id", m.getId());
    node.put("nickname", m.getNickname());
    return node;
  }
}
