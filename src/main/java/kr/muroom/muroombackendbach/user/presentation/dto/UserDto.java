package kr.muroom.muroombackendbach.user.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public class UserDto {

  private UserDto() {
  }

  public record SmsSendRequest(
      @NotBlank
      String phone
  ) {

  }

  public record SmsVerifyRequest(
      @NotBlank
      String phone,

      @NotBlank
      String code
  ) {

  }

  public record SmsVerifyResponse(
      boolean success
  ) {

  }

  public record NicknameCheckResponse(
      boolean available
  ) {

  }

  public record NicknameResponse(
      String nickname
  ) {

  }
}
