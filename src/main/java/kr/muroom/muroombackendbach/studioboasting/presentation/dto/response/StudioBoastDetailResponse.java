package kr.muroom.muroombackendbach.studioboasting.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.OffsetDateTime;
import java.util.List;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayStationInfo;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioListElementResponse;
import lombok.Builder;

@Schema(description = "작업실 소개(자랑) 상세 조회 응답 DTO")
@Builder
public record StudioBoastDetailResponse(
    @Schema(description = "작업실 소개(자랑) 게시글 ID", example = "123456789012345678", requiredMode = RequiredMode.REQUIRED)
    String id,

    @Schema(description = "작업실 소개(자랑) 게시글 내용", example = "우리 스튜디오에서 멋진 음악 작업하세요!", requiredMode = RequiredMode.REQUIRED)
    String content,

    @Schema(description = "작업실 소개(자랑) 이미지 파일 URL 목록", requiredMode = RequiredMode.REQUIRED)
    List<String> imageFileUrls,

    @Schema(description = "요청한 사용자가 좋아요를 눌렀는지 여부", example = "true", requiredMode = RequiredMode.REQUIRED)
    boolean isLikedByRequestUser,

    @Schema(description = "좋아요 수", example = "150", requiredMode = RequiredMode.REQUIRED)
    long likeCount,

    @Schema(description = "댓글 수", example = "25", requiredMode = RequiredMode.REQUIRED)
    long commentCount,

    @Schema(description = "작성일시", example = "2024-06-15T14:30:00+09:00", requiredMode = RequiredMode.REQUIRED)
    OffsetDateTime createdAt,

    @Schema(description = "서비스에 업로드된 스튜디오인지 여부", example = "true", requiredMode = RequiredMode.REQUIRED)
    boolean isStudioUploaded,

    @Schema(description = "작성자 정보", requiredMode = RequiredMode.REQUIRED)
    CreatorUserInfo creatorUserInfo,

    @Schema(description = "(unknownStudioInfo가 null일 때만) 서비스에 업로드된 스튜디오인 경우 muroom 스튜디오 정보", nullable = true)
    StudioInfo studioInfo,

    @Schema(description = "(studioInfo가 null일 때만) 서비스에 업로드되지 않은 스튜디오인 경우 사용자 등록 정보", nullable = true)
    UnknownStudioInfo unknownStudioInfo
) {

  @Builder
  public record CreatorUserInfo(
      @Schema(description = "작성자 musician ID", example = "790273425936465074", nullable = true)
      String id,

      @Schema(description = "작성자 닉네임", example = "뮤룸작가", requiredMode = RequiredMode.REQUIRED)
      String nickname,

      @Schema(description = "작성자 대표 악기", example = "GUITAR", requiredMode = RequiredMode.REQUIRED)
      String instrument,

      @Schema(description = "작성자의 인스타그램 계정(이벤트 기간 한정)", example = "my_insta", nullable = true)
      String instagramAccount
  ) {

  }

  @Builder
  public record StudioInfo(
      @Schema(description = "작업실(스튜디오) ID", example = "791543436721219205", nullable = true)
      String id,

      @Schema(description = "작업실(스튜디오) 이름", example = "뮤룸 스튜디오 홍대점", requiredMode = RequiredMode.REQUIRED)
      String name,

      @Schema(description = "작업실(스튜디오) 썸네일 이미지 파일 URL", requiredMode = RequiredMode.REQUIRED)
      String thumbnailImageFileUrl,

      @Schema(description = "인근 지하철역 정보", requiredMode = RequiredMode.REQUIRED)
      StudioSubwayStationInfo nearestSubwayStation,

      @Schema(description = "작업실(스튜디오) 최소 가격", example = "150000", nullable = true)
      Integer minPrice,

      @Schema(description = "작업실(스튜디오) 최대 가격", example = "430000", nullable = true)
      Integer maxPrice
  ) {

    public static StudioInfo from(StudioListElementResponse studioListElement) {
      return StudioInfo.builder()
          .id(studioListElement.studioId())
          .name(studioListElement.studioName())
          .thumbnailImageFileUrl(studioListElement.thumbnailImageUrl())
          .nearestSubwayStation(studioListElement.nearbySubwayStationInfo())
          .minPrice(studioListElement.minPrice())
          .maxPrice(studioListElement.maxPrice())
          .build();
    }
  }

  @Builder
  public record UnknownStudioInfo(
      @Schema(description = "작업실(스튜디오) 이름", example = "뮤룸 스튜디오 홍대점", requiredMode = RequiredMode.REQUIRED)
      String name,

      @Schema(description = "인근 지하철역 정보", requiredMode = RequiredMode.REQUIRED)
      StudioSubwayStationInfo nearestSubwayStation,

      @Schema(description = "도로명 주소", example = "서울특별시 마포구 양화로 123", requiredMode = RequiredMode.REQUIRED)
      String roadNameAddress,

      @Schema(description = "지번 주소", example = "서울특별시 마포구 서교동 123-45", requiredMode = RequiredMode.REQUIRED)
      String lotNumberAddress,

      @Schema(description = "상세 주소", example = "101호", requiredMode = RequiredMode.REQUIRED)
      String detailedAddress
  ) {

  }
}
