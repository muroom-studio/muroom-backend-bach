package kr.muroom.muroombackendbach.instrument.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InstrumentErrorCode implements ErrorCode {
  NOT_EXIST_INSTRUMENT(HttpStatus.BAD_REQUEST, "IS-400-02", "존재하지 않는 악기 입니다.");
  private final HttpStatus status;
  private final String code;
  private final String message;

}