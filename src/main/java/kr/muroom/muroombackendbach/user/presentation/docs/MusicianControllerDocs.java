package kr.muroom.muroombackendbach.user.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginRequest;
import kr.muroom.muroombackendbach.auth.oauth.login.dto.OAuthLoginResponse;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.request.LogoutRequest;
import kr.muroom.muroombackendbach.user.presentation.dto.request.MusicianSignupRequest;
import kr.muroom.muroombackendbach.user.presentation.dto.request.UpdateMusicianProfileRequest;
import kr.muroom.muroombackendbach.user.presentation.dto.response.MusicianProfileResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.response.MusicianSignupResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.response.MusicianSimpleProfileResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "뮤지션 API", description = "뮤지션 관련 API")
public interface MusicianControllerDocs {

  @Operation(
      summary = "뮤지션 회원가입",
      description = "뮤지션 회원 정보를 등록합니다."
  )
  ApiResponse<MusicianSignupResponse> registerMusician(
      @Valid @RequestBody MusicianSignupRequest request
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
      description = """
          Swagger 테스트 전용 로그인 API입니다.  
          운영/개발 환경에서는 redirect 기반 로그인 플로우를 사용해야 합니다.
          
          아래 카카오 로그인 페이지에서 인증 후,
          리다이렉트 URL에 포함된 `code` 값을 `providerId` 필드에 입력해주세요.
          
          🔗 카카오 로그인 페이지  
          [카카오 로그인 바로가기](https://kauth.kakao.com/oauth/authorize?client_id=a87a624a98805882ce612eed7c018237&redirect_uri=http://localhost:3001/redirect/oauth/kakao&response_type=code)
          """
  )
  ApiResponse<OAuthLoginResponse> oauthLoginForSwagger(
      @Valid @RequestBody OAuthLoginRequest request,
      @Parameter(
          description = "요청이 발생한 origin (Swagger 테스트용)",
          example = "http://localhost:3001",
          schema = @Schema(defaultValue = "http://localhost:3001")
      )
      @RequestParam String origin
  );

  @Operation(
      summary = "로그아웃",
      description =
          """
              현재 로그인한 뮤지션의 소셜 토큰을 만료(삭제)합니다.
              클라이언트는 JWT를 로컬에서 삭제해야 합니다.
              
              - 요청 쿠키(refresh)가 존재하면 해당 Refresh Token을 서버에서 폐기합니다.
              - Refresh Token의 소유자가 현재 로그인 사용자와 다르면 요청이 거부됩니다.
              """
  )
  @SecurityRequirement(name = "Authentication")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "로그아웃 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "리프레시 토큰 소유자 불일치",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = {
                  @ExampleObject(
                      name = "Refresh Token 소유자 불일치",
                      value = """
                          {
                            "code": "JWT-403-01",
                            "message": "리프레시 토큰의 소유자가 아닙니다."
                          }
                          """,
                      description =
                          """
                              - 쿠키(refresh)로 전달된 Refresh Token의 musicianId(소유자)와
                                현재 인증된 사용자(@AuthenticationPrincipal)의 musicianId가 다른 경우
                              """
                  )
              }
          )
      )
  })
  @SecurityRequirement(name = "Authentication")
  ApiResponse<Void> logout(
      @AuthenticationPrincipal Long musicianId,
      @RequestBody(required = false) LogoutRequest request
  );

  @Operation(
      summary = "내 간략 정보 조회",
      description = "현재 로그인한 뮤지션의 간략 정보(프로필 이미지, 닉네임)를 조회합니다."
  )
  @SecurityRequirement(name = "Authentication")
  ApiResponse<MusicianSimpleProfileResponse> getMySimpleProfile(
      @AuthenticationPrincipal Long musicianId
  );

  @Operation(
      summary = "내 상세 프로필 조회",
      description =
          """
              현재 로그인한 뮤지션의 상세 프로필 정보를 조회합니다.
              
              포함 정보:
              - musicianId
              - nickname
              - instrument
              - snsAccount(provider)
              - myStudio(나의 작업실)
              """
  )
  @SecurityRequirement(name = "Authentication")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "내 상세 프로필 조회 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "뮤지션을 찾을 수 없음",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = {
                  @ExampleObject(
                      name = "뮤지션 없음",
                      value = """
                          {
                            "code": "MC-400-02",
                            "message": "뮤지션을 찾을 수 없습니다."
                          }
                          """,
                      description = "musicianId로 뮤지션 조회에 실패한 경우"
                  ),
                  @ExampleObject(
                      name = "소셜 계정 없음",
                      value = """
                          {
                            "code": "SA-400-01",
                            "message": "존재하지 않는 소셜 계정입니다."
                          }
                          """,
                      description = "musicianId로 소셜 계정 조회에 실패한 경우"
                  ),
                  @ExampleObject(
                      name = "나의 작업실 정보가 없음",
                      value = """
                          {
                            "code": "MS-400-01",
                            "message": "존재하지 않는 나의 작업실입니다."
                          }
                          """,
                      description = "musicianId로 나의 작업실 조회에 실패한 경우"
                  ),
                  @ExampleObject(
                      name = "나의 작업실 정보가 없음",
                      value = """
                          {
                            // HTTP STATUS가 403으로 온다면 무조건 로그인으로.
                            "status": "403",
                          }
                          """,
                      description = "로그인 필요"
                  )
              }
          )
      )
  })
  ApiResponse<MusicianProfileResponse> getMyProfile(
      @AuthenticationPrincipal Long musicianId
  );

  @SecurityRequirement(name = "Authentication")
  @Operation(
      summary = "내 상세 프로필 수정",
      description = """
          내 상세 프로필을 부분 수정합니다. (전달된 필드만 변경)
          """
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "내 상세 프로필 수정 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청/리소스 조회 실패",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = {
                  @ExampleObject(
                      name = "뮤지션 없음",
                      value = """
                          {
                            "code": "MC-400-02",
                            "message": "뮤지션을 찾을 수 없습니다."
                          }
                          """,
                      description = "musicianId로 뮤지션 조회에 실패한 경우"
                  ),
                  @ExampleObject(
                      name = "존재하지 않는 악기",
                      value = """
                          {
                            "code": "IS-400-02",
                            "message": "존재하지 않는 악기입니다."
                          }
                          """,
                      description = "instrumentId로 악기 조회에 실패한 경우"
                  ),
                  @ExampleObject(
                      name = "전화번호 중복",
                      value = """
                          {
                            "code": "MC-400-03",
                            "message": "이미 사용 중인 전화번호입니다."
                          }
                          """,
                      description = "변경하려는 전화번호가 이미 존재하는 경우"
                  ),
                  @ExampleObject(
                      name = "나의 작업실 정보가 없음",
                      value = """
                          {
                            "code": "MS-400-01",
                            "message": "존재하지 않는 나의 작업실입니다."
                          }
                          """,
                      description = "musicianId로 나의 작업실 조회에 실패한 경우"
                  )
              }
          )
      )
  })
  ApiResponse<Void> updateMyProfile(
      @AuthenticationPrincipal Long musicianId,
      @RequestBody UpdateMusicianProfileRequest request
  );

}