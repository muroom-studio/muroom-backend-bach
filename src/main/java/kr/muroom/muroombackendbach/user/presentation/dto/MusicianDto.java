package kr.muroom.muroombackendbach.user.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public final class MusicianDto {

  private MusicianDto() {
  }

  public record MusicianSignUpDto(
      @NotBlank(message = "이름을 입력해주세요.")
      String name,

      @NotNull(message = "생년월일을 입력해주세요.")
      LocalDate birthdate,

      @NotBlank(message = "전화번호를 입력해주세요.")
      String phoneNumber,

      @NotBlank(message = "닉네임을 입력해주세요.")
      String nickname,

      @NotNull(message = "악기 정보를 선택해주세요.")
      Long instrumentId,

      @NotEmpty(message = "약관 동의 항목을 선택해주세요.")
      List<Long> termIds,

      @NotBlank(message = "회원가입 토큰이 없습니다.")
      String signupToken

  ) {

  }

  public record MusicianSignUpResponse(
      String accessToken,
      Long musicianId) {

  }
}
