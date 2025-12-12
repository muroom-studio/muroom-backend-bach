package kr.muroom.muroombackendbach.user.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto;
import kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto.MusicianSignUpResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto.MusicianSimpleProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "뮤지션 API", description = "뮤지션 관련 API")
public interface MusicianControllerDocs {

  @Operation(
      summary = "뮤지션 회원가입",
      description = "뮤지션 회원 정보를 등록합니다."
  )
  ApiResponse<MusicianSignUpResponse> registerMusician(
      @Valid @RequestBody MusicianDto.MusicianSignUpDto request
  );

  @Operation(
      summary = "뮤지션 로그인",
      description = "인가 코드를 기반으로 소셜 로그인을 시도합니다."
  )
  ApiResponse<OAuthLoginResponse> oauthLogin(
      @Valid @RequestBody OAuthLoginRequest request,
      @RequestHeader(value = "Origin") String origin
  );

  @Operation(
      summary = "뮤지션 로그인 (Swagger 테스트용)",
      description = "Swagger 테스트 전용 로그인 API입니다. "
          + "운영/개발 환경에서는 redirect 기반 로그인 플로우를 사용해야 합니다."
  )
  ApiResponse<OAuthLoginResponse> oauthLoginForSwagger(
      @Valid @RequestBody OAuthLoginRequest request,
      @RequestParam String origin
  );

  @Operation(
      summary = "로그아웃",
      description = "현재 로그인한 뮤지션의 소셜 토큰을 만료(삭제)합니다. "
          + "클라이언트는 JWT를 로컬에서 삭제해야 합니다."
  )
  @SecurityRequirement(name = "Authentication")
  ApiResponse<Void> logout(
      @AuthenticationPrincipal Long musicianId
  );

  @Operation(
      summary = "내 간략 정보 조회",
      description = "현재 로그인한 뮤지션의 간략 정보(프로필 이미지, 닉네임)를 조회합니다."
  )
  @SecurityRequirement(name = "Authentication")
  ApiResponse<MusicianSimpleProfileResponse> getMySimpleProfile(
      @AuthenticationPrincipal Long musicianId
  );
}