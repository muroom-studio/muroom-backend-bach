package kr.muroom.muroombackendbach.instrument.presentation.dto;

import kr.muroom.muroombackendbach.instrument.domain.entity.Instrument;

public final class InstrumentDto {

  private InstrumentDto() {
  }

  public record InstrumentResponse(
      Long id,
      String code,
      String description
  ) {

    public static InstrumentResponse from(Instrument instrument) {
      return new InstrumentResponse(
          instrument.getId(),
          instrument.getCode(),
          instrument.getDescription()
      );
    }
  }

}
