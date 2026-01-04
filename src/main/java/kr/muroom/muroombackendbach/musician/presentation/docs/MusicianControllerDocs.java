package kr.muroom.muroombackendbach.musician.presentation.docs;

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
import kr.muroom.muroombackendbach.musician.presentation.dto.request.MusicianSignupRequest;
import kr.muroom.muroombackendbach.musician.presentation.dto.response.MusicianNicknameCheckResponse;
import kr.muroom.muroombackendbach.musician.presentation.dto.response.MusicianProfileResponse;
import kr.muroom.muroombackendbach.musician.presentation.dto.response.MusicianSignupResponse;
import kr.muroom.muroombackendbach.musician.presentation.dto.response.MusicianSimpleProfileResponse;
import kr.muroom.muroombackendbach.auth.auth.presentation.dto.request.LogoutRequest;
import kr.muroom.muroombackendbach.musician.presentation.dto.request.UpdateMusicianProfileRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "musician - 뮤지션 API")
public interface MusicianControllerDocs {

  @Operation(
      summary = "뮤지션 회원가입",
      description = "뮤지션 회원 정보를 등록합니다."
  )
  ApiResponse<MusicianSignupResponse> registerMusician(
      @Valid @RequestBody MusicianSignupRequest request
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

  @Operation(
      summary = "닉네임 중복 확인",
      description = """
          닉네임 사용 가능 여부를 확인합니다.
          
          - available = true  : 사용 가능
          - available = false : 이미 사용 중
          
          예외는 발생하지 않습니다.
          """
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "닉네임 중복 확인 성공"
      )
  })
  ApiResponse<MusicianNicknameCheckResponse> checkNickname(
      @Parameter(
          description = "검사할 닉네임",
          example = "muroom_artist"
      )
      @RequestParam String nickname
  );

  @Operation(
      summary = "전화번호 중복 확인",
      description = """
          전화번호 사용 가능 여부를 확인합니다.
          
          - 사용 가능한 경우: 200 OK
          - 이미 존재하는 전화번호: 409 CONFLICT
          - 잘못된 전화번호 형식: 400 BAD_REQUEST
          """
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "전화번호 사용 가능"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "409",
          description = "이미 존재하는 전화번호",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  name = "전화번호 중복",
                  value = """
                      {
                        "code": "US-409-03",
                        "message": "이미 존재하는 전화번호 입니다."
                      }
                      """
              )
          )
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 전화번호 형식",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = @ExampleObject(
                  name = "잘못된 핸드폰 번호",
                  value = """
                      {
                        "code": "SM-400-01",
                        "message": "잘못된 핸드폰 번호입니다."
                      }
                      """
              )
          )
      )
  })
  ApiResponse<Void> checkPhone(
      @Parameter(
          description = "검사할 전화번호 (하이픈 없이)",
          example = "01012345678"
      )
      @RequestParam String phone
  );

}