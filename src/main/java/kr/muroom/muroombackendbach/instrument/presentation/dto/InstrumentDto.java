package kr.muroom.muroombackendbach.instrument.presentation.dto;

import kr.muroom.muroombackendbach.instrument.domain.entity.Instrument;

public final class InstrumentDto {

  private InstrumentDto() {
  }

  public record InstrumentResponse(
      String id,
      String code,
      String description
  ) {

    public static InstrumentResponse from(Instrument instrument) {
      return new InstrumentResponse(
          String.valueOf(instrument.getId()),
          instrument.getCode(),
          instrument.getDescription()
      );
    }
  }

}
