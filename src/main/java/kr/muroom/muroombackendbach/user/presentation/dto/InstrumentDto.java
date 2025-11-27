package kr.muroom.muroombackendbach.user.presentation.dto;

import kr.muroom.muroombackendbach.user.domain.entity.Instrument;

public final class InstrumentDto {

  private InstrumentDto() {
  }

  public record InstrumentResponse(
      Long id,
      String name
  ) {

    public static InstrumentResponse from(Instrument instrument) {
      return new InstrumentResponse(
          instrument.getId(),
          instrument.getName()
      );
    }
  }

}
