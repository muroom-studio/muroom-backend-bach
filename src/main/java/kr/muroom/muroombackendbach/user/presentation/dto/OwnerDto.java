package kr.muroom.muroombackendbach.user.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

public class OwnerDto {

  private OwnerDto() {
  }

  public record OwnerSignUpDto(
      String name,
      String email,
      String password,
      LocalDate birthdate,
      String phoneNumber,
      String nickname,
      List<Long> termIds
  ) {

  }

  public record OwnerLoginRequest(
      String email,
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
