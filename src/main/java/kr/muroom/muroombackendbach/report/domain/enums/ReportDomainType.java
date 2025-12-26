package kr.muroom.muroombackendbach.report.domain.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import kr.muroom.muroombackendbach.common.domain.EnumMapperType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ReportDomainType implements EnumMapperType {

  /* ===== 사람 ===== */

  /**
   * 사용자(뮤지션) 계정 자체에 대한 신고 예) 프로필 허위 정보, 부적절한 닉네임, 반복적인 악성 행위
   */
  MUSICIAN("사용자(뮤지션) 계정"),

  /**
   * 스튜디오 운영자 계정 신고 예) 허위 매물 등록, 운영자 부정 행위
   */
  STUDIO_OWNER("스튜디오 운영자 계정"),

  /* ===== 장소 / 리소스 ===== */

  /**
   * 스튜디오 매물 자체 예) 실제와 다른 정보, 허위 시설 설명
   */
  STUDIO("스튜디오 매물"),

  /**
   * 매물 자랑 작성글 신고
   */
  STUDIO_BOAST("스튜디오 매물 자랑"),

  /**
   * 스튜디오 내부 개별 룸 예) 사진/설명과 다른 룸 상태
   */
  ROOM("스튜디오 룸"),

  /* ===== 게시글 / 커뮤니티 ===== */

  /**
   * 일반 게시글 예) 광고성 게시글, 불법 정보 게시
   */
  POST("게시글"),

  /**
   * 댓글 예) 욕설, 혐오 발언, 특정 대상 비방
   */
  COMMENT("댓글"),

  /**
   * 후기(리뷰) 예) 허위 후기, 악의적인 비방
   */
  REVIEW("후기"),

  /**
   * 공지사항 예) 부적절한 운영/시스템 공지
   */
  NOTICE("공지사항"),

  /* ===== 미디어 ===== */

  /**
   * 업로드된 이미지 예) 음란 이미지, 저작권 침해
   */
  IMAGE("이미지"),

  /**
   * 업로드된 영상 예) 불법 촬영물, 폭력적 영상
   */
  VIDEO("영상"),

  /* ===== 커뮤니케이션 ===== */

  /**
   * 채팅 메시지 예) 스팸, 욕설, 성희롱
   */
  CHAT_MESSAGE("채팅 메시지");

  private final String description;

  @Override
  public String getCode() {
    return name();
  }
}
