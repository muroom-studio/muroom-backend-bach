package kr.muroom.muroombackendbach.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import kr.muroom.muroombackendbach.instrument.domain.entity.Instrument;
import kr.muroom.muroombackendbach.user.domain.entity.MyStudio;
import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
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
      String refreshToken,
      Long musicianId) {

  }

  @Builder
  public record MusicianProfileResponse(
      @Schema(description = "뮤지션 ID", example = "1")
      Long musicianId,

      @Schema(description = "닉네임", example = "뮤루뮤루")
      String nickname,

      @Schema(description = "나의 악기 정보")
      InstrumentSimpleInfo musicianInstrument,

      @Schema(description = "나의 작업실 정보")
      MyStudioInfo myStudio,

      @Schema(
          name = "snsAccount",
          description = "소셜 로그인 제공자 (code + description 형태로 응답됩니다.)",
          example = """
              { "code": "KAKAO", "description": "카카오" }
              """
      )
      OAuthProvider snsAccount
  ) {

  }

  @Builder
  public record MusicianSimpleProfileResponse(
      @Schema(description = "뮤지션 ID", example = "1")
      Long musicianId,

      @Schema(description = "닉네임", example = "뮤루뮤루")
      String nickname,

      @Schema(description = "나의 악기 정보")
      InstrumentSimpleInfo musicianInstrument
  ) {

  }

  @Builder
  public record InstrumentSimpleInfo(
      @Schema(description = "악기 코드", example = "VOCAL")
      String code,
      @Schema(description = "악기 이름", example = "보컬")
      String description
  ) {

    public static InstrumentSimpleInfo from(Instrument instrument) {
      return InstrumentSimpleInfo.builder()
          .code(instrument.getCode())
          .description(instrument.getDescription())
          .build();
    }
  }

  @Builder
  public record MyStudioInfo(
      @Schema(description = "작업실 이름", example = "연습실")
      String name,

      @Schema(description = "도로명 주소", example = "서울 관악구 남부순환로218길 32 (봉천동, 우남하우정Ⅱ)")
      String roadAddress,

      @Schema(description = "상세 주소", example = "209호")
      String detailAddress
  ) {

    public static MyStudioInfo from(MyStudio myStudio) {
      return MyStudioInfo.builder()
          .name(myStudio.getName())
          .roadAddress(myStudio.getRoadAddress())
          .detailAddress(myStudio.getDetailAddress())
          .build();
    }
  }
}
