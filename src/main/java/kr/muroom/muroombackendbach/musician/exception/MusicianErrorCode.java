package kr.muroom.muroombackendbach.musician.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MusicianErrorCode implements ErrorCode {
  MUSICIAN_NOT_FOUND(HttpStatus.BAD_REQUEST, "MC-400-02", "존재하지 않는 뮤지션입니다."),
  ALREADY_EXIST_NICKNAME(HttpStatus.BAD_REQUEST, "MC-400-04", "이미 존재하는 닉네임 입니다."),
  ;
  private final HttpStatus status;
  private final String code;
  private final String message;

}
