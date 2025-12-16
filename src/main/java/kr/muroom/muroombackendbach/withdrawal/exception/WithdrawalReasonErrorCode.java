package kr.muroom.muroombackendbach.withdrawal.exception;

import kr.muroom.muroombackendbach.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WithdrawalReasonErrorCode implements ErrorCode {
  NOT_EXIST_WITHDRAWAL_REASON(HttpStatus.BAD_REQUEST, "WR-400-01", "존재하지 않는 탈퇴 사유입니다.");
  private final HttpStatus status;
  private final String code;
  private final String message;
}
