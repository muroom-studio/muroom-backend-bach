package kr.muroom.muroombackendbach.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class OwnerDto {

  private OwnerDto() {
  }

  @Schema(
      name = "사장님 로그인 요청 DTO",
      description = "사장님 로그인 요청 DTO"
  )
  public record OwnerLoginRequest(
      @NotBlank(message = "이메일을 입력해주세요.")
      @Email(message = "이메일 형식이 올바르지 않습니다.")
      @Schema(example = "owner@example.com")
      String email,

      @NotBlank
      @Schema(example = "password1234")
      String password
  ) {

  }

  @Schema(
      name = "이메일 중복 확인 요청 DTO",
      description = "이메일 중복 확인 요청 DTO"
  )
  public record EmailCheckRequest(
      @NotBlank(message = "이메일을 입력하세요.")
      @Email(message = "이메일 형식이 올바르지 않습니다.")
      @Schema(example = "owner@example.com")
      String email
  ) {

  }

  @Schema(
      name = "이메일 중복 확인 응답 DTO",
      description = "이메일 중복 확인 응답 DTO"
  )
  public record EmailCheckResponse(
      @Schema(example = "true")
      boolean available,

      @Schema(example = "사용 가능한 이메일입니다.")
      String message
  ) {

  }
}
