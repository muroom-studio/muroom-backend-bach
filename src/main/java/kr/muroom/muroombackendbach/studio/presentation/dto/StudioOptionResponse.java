package kr.muroom.muroombackendbach.studio.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

public final class StudioOptionResponse {

  private StudioOptionResponse() {
  }

  @Builder
  public record GetAll(
      @Schema(description = "건물 유형 - 지층 옵션")
      List<GetSingle> floorOptions,

      @Schema(description = "건물 유형 - 화장실 옵션")
      List<GetSingle> restroomOptions,

      @Schema(description = "스튜디오 공용 옵션")
      List<GetSingle> studioCommonOptions,

      @Schema(description = "스튜디오 개인 옵션")
      List<GetSingle> studioIndividualOptions,

      @Schema(description = "이용 불가 악기 옵션")
      List<GetSingle> unavailableInstrumentOptions
  ) {

  }

  @Builder
  @Schema(description = "옵션 DTO")
  public record GetSingle(
      @Schema(description = "옵션 코드", example = "BASEMENT")
      String code,
      @Schema(description = "옵션 이름 및 표시값", example = "지하")
      String description
  ) {

  }
}
