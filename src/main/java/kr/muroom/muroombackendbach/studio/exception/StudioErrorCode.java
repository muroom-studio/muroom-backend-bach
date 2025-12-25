package kr.muroom.muroombackendbach.studio.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StudioErrorCode implements ErrorCode {
  STUDIO_NOT_FOUND(HttpStatus.BAD_REQUEST, "ST-404-01", "해당 스튜디오를 찾을 수 없습니다."),
  RESTROOM_DETAIL_IS_EMPTY(HttpStatus.BAD_REQUEST, "ST-400-02", "화장실이 있는 경우, 화장실 상세 정보는 필수입니다."),
  INVALID_PRICE_RANGE(HttpStatus.BAD_REQUEST, "ST-400-03", "잘못된 가격 범위입니다."),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;

}
