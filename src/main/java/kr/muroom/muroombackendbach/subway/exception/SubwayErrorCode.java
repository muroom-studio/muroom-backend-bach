package kr.muroom.muroombackendbach.subway.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SubwayErrorCode implements ErrorCode {
  SUBWAY_NOT_FOUND(HttpStatus.NOT_FOUND, "SB_404-01", "지하철 정보를 찾을 수 없습니다."),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}
