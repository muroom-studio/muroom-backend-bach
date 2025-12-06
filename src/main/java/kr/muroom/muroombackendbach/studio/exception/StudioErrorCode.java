package kr.muroom.muroombackendbach.studio.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum StudioErrorCode implements ErrorCode {
  STUDIO_NOT_FOUND(HttpStatus.BAD_REQUEST, "ST-400-01", "해당 스튜디오를 찾을 수 없습니다."),
  RESTROOM_DETAIL_IS_EMPTY(HttpStatus.BAD_REQUEST, "ST-400-02", "화장실 세부 정보는 화장실이 있는 경우에만 제공될 수 있습니다."),
  ;

  private final HttpStatus status;
  private final String code;
  private final String message;

}
