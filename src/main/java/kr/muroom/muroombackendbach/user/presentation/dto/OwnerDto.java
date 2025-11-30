package kr.muroom.muroombackendbach.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

public class OwnerDto {

  private OwnerDto() {
  }

  public record OwnerSignUpDto(
      @NotBlank(message = "이름을 입력해주세요.")
      String name,
      @NotBlank(message = "이메일을 입력해주세요.")
      @Email(message = "이메일 형식이 올바르지 않습니다.")
      String email,
      @NotBlank
      String password,
      LocalDate birthdate,
      String phoneNumber,
      @NotBlank(message = "닉네임을 입력해주세요.")
      String nickname,
      List<Long> termIds
  ) {

  }

  public record OwnerLoginRequest(
      @NotBlank(message = "이메일을 입력해주세요.")
      @Email(message = "이메일 형식이 올바르지 않습니다.")
      String email,
      @NotBlank
      String password
  ) {

  }

  public record EmailCheckRequest(
      @NotBlank(message = "이메일을 입력하세요.")
      @Email(message = "이메일 형식이 올바르지 않습니다.")
      String email
  ) {

  }

  public record EmailCheckResponse(
      boolean available,
      String message
  ) {

  }

}
