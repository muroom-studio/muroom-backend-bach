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

  public record VerifyRequest(
      @NotBlank
      String phoneNumber,

      @NotBlank
      String code
  ) {

  }

  public record VerifyResponse(
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
