package kr.muroom.muroombackendbach.musician.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.muroom.muroombackendbach.auth.auth.domain.entity.OAuthProvider;
import kr.muroom.muroombackendbach.musician.domain.entity.MyStudio;
import kr.muroom.muroombackendbach.musician.presentation.dto.response.MusicianSimpleProfileResponse.InstrumentSimpleInfo;
import lombok.Builder;

@Builder
public record MusicianProfileResponse(
    @Schema(description = "뮤지션 ID", example = "791543436721219205")
    String musicianId,

    @Schema(description = "닉네임", example = "뮤루뮤루")
    String nickname,

    @Schema(description = "010-1111-2222")
    String phone,

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

  @Builder
  public record MyStudioInfo(
      @Schema(description = "작업실 이름", example = "연습실")
      String name,

      @Schema(description = "도로명 주소", example = "서울 관악구 남부순환로218길 32 (봉천동, 우남하우정Ⅱ)")
      String roadAddress,

      @Schema(description = "상세 주소", example = "209호")
      String detailAddress
  ) {

  }
}
