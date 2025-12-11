package kr.muroom.muroombackendbach.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class UserDto {

  private UserDto() {
  }

  @Schema(name = "문자 인증 전송 요청", description = "SMS 인증번호 전송 요청 DTO")
  public record SmsSendRequest(
      @Schema(description = "전화번호(하이픈 붙여야함)", example = "010-1234-5678")
      @NotBlank
      String phone
  ) {

  }

  @Schema(name = "문자 인증 검증", description = "SMS 인증번호 검증 요청 DTO")
  public record SmsVerifyRequest(
      @Schema(description = "인증코드 받았던 전화번호", example = "010-1234-5678")
      @NotBlank
      String phone,

      @Schema(description = "인증번호 6자리", example = "123456")
      @NotBlank
      String code
  ) {

  }

  @Schema(name = "인증 검증 성공 유무 응답", description = "SMS 인증번호 검증 결과 응답 DTO")
  public record SmsVerifyResponse(

      @Schema(description = "인증 성공 여부", example = "true")
      boolean success
  ) {

  }

  @Schema(name = "닉네임 사용 가능 여부 응답 DTO")
  public record NicknameCheckResponse(

      @Schema(description = "사용 가능 여부", example = "true")
      boolean available
  ) {

  }

  @Schema(name = "사용자의 닉네임 응답 DTO")
  public record NicknameResponse(
      @Schema(description = "닉네임", example = "뮤룸유저123")
      String nickname
  ) {

  }
}
