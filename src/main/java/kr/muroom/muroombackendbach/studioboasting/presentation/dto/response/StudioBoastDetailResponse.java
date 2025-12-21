package kr.muroom.muroombackendbach.studioboasting.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.OffsetDateTime;
import java.util.List;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioInfo.StudioSubwayStationInfo;
import lombok.Builder;

@Builder
public record StudioBoastDetailResponse(
    Long id,
    String content,
    List<String> imageFileKeys,
    long likeCount,
    long commentCount,
    OffsetDateTime createdAt,

    @Schema(description = "서비스에 업로드된 스튜디오인지 여부", example = "true", requiredMode = RequiredMode.REQUIRED)
    boolean isStudioUploaded,

    @Schema(description = "작성자 정보", requiredMode = RequiredMode.REQUIRED)
    CreatorUserInfo creatorUserInfo,

    @Schema(description = "서비스에 업로드된 스튜디오인 경우 muroom 스튜디오 정보", nullable = true)
    StudioInfo studioInfo,

    @Schema(description = "서비스에 업로드되지 않은 스튜디오인 경우 사용자 등록 정보", nullable = true)
    UnknownStudioInfo unknownStudioInfo
) {

  @Builder
  public record CreatorUserInfo(
      @Schema(description = "작성자 musician ID", example = "790273425936465074", nullable = true)
      Long id,

      @Schema(description = "작성자 닉네임", example = "뮤룸작가", requiredMode = RequiredMode.REQUIRED)
      String nickname,

      @Schema(description = "작성자 대표 악기", example = "GUITAR", requiredMode = RequiredMode.REQUIRED)
      String instrument
  ) {

  }

  @Builder
  public record StudioInfo(
      @Schema(description = "작업실(스튜디오) ID", example = "1", nullable = true)
      Long id,

      @Schema(description = "작업실(스튜디오) 이름", example = "뮤룸 스튜디오 홍대점", requiredMode = RequiredMode.REQUIRED)
      String name,

      @Schema(description = "작업실(스튜디오) 썸네일 이미지 파일 키", example = "studio-boasts/abcd-efgh-ijkl.png", nullable = true)
      String thumbnailImageFileKey,

      @Schema(description = "인근 지하철역 정보", requiredMode = RequiredMode.REQUIRED)
      StudioSubwayStationInfo nearestSubwayStation,

      @Schema(description = "작업실(스튜디오) 최소 가격", example = "150000", nullable = true)
      Integer minPrice,

      @Schema(description = "작업실(스튜디오) 최대 가격", example = "430000", nullable = true)
      Integer maxPrice
  ) {

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
