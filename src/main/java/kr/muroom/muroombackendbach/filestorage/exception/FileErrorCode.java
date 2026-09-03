package kr.muroom.muroombackendbach.filestorage.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {
  UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "FS-400-01", "지원하지 않는 파일 형식입니다."),
  INVALID_FILE_KEY(HttpStatus.BAD_REQUEST, "FS-400-02", "[서버 확인 필요] 유효하지 않은 파일 키입니다."),
  INVALID_FILE_PATH(HttpStatus.BAD_REQUEST, "FS-400-3", "유효하지 않은 파일 경로입니다."),
  FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FS-404-01", "[서버 확인 필요] 해당 파일을 찾지 못했습니다."),

  INVALID_TEMP_FILE_KEY(HttpStatus.BAD_REQUEST, "FS-400-12", "유효하지 않은 임시 파일 키입니다."),

  ;

  private final HttpStatus status;
  private final String code;
  private final String message;
}
