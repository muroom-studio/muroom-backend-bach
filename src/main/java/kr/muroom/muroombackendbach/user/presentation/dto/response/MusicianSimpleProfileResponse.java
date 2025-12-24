package kr.muroom.muroombackendbach.user.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.muroom.muroombackendbach.instrument.domain.entity.Instrument;
import lombok.Builder;

@Builder
public record MusicianSimpleProfileResponse(
    @Schema(description = "뮤지션 ID", example = "1")
    String musicianId,

    @Schema(description = "닉네임", example = "뮤루뮤루")
    String nickname,

    @Schema(description = "나의 악기 정보")
    InstrumentSimpleInfo musicianInstrument
) {

  @Builder
  public record InstrumentSimpleInfo(
      @Schema(description = "악기 코드", example = "VOCAL")
      String code,
      @Schema(description = "악기 이름", example = "보컬")
      String description
  ) {

    public static InstrumentSimpleInfo from(Instrument instrument) {
      return InstrumentSimpleInfo.builder()
          .code(instrument.getCode())
          .description(instrument.getDescription())
          .build();
    }
  }
}
