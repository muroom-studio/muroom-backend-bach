package kr.muroom.muroombackendbach.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;

public final class MusicianDto {

  private MusicianDto() {
  }

  public record MusicianSignUpDto(
      @NotBlank(message = "이름을 입력해주세요.")
      String name,

      @NotBlank(message = "전화번호를 입력해주세요.")
      String phoneNumber,

      String detailJuso,

      String juso,

      String studioName,

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

  // TODO 약관 조회 유효성 검사를 위한 DTO 변경 필요
  public record TermRequest(
      Long termId,
      Boolean agreed
  ) {

  }

  public record MusicianSignUpResponse(
      String accessToken,
      Long musicianId) {

  }

  @Builder
  public record MusicianSimpleProfileResponse(
      @Schema(description = "뮤지션 ID", example = "1")
      Long musicianId,

      @Schema(description = "닉네임", example = "뮤루뮤루")
      String nickname,

      @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
      String profileImageUrl
  ) {

  }
}
