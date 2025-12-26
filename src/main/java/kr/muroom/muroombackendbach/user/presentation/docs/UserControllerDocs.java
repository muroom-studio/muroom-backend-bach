package kr.muroom.muroombackendbach.user.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import kr.muroom.muroombackendbach.auth.jwt.RefreshTokenService.TokenPair;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.sms.presentation.dto.SmsAuthResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.UserDto;
import kr.muroom.muroombackendbach.user.presentation.dto.UserDto.NicknameCheckResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.UserDto.SmsVerifyRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "user - 유저(뮤지션/사장님) 공용 API")
public interface UserControllerDocs {

  @Operation(
      summary = "닉네임 중복 확인",
      description = "회원가입 또는 프로필 설정 시 사용 가능한 닉네임인지 확인합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "닉네임 사용 가능 여부 반환"
      )
  })
  ApiResponse<NicknameCheckResponse> checkNickname(@RequestParam String nickname);

  @Operation(summary = "SMS 인증번호 발송", description = "입력한 휴대폰 번호로 인증번호를 발송합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description =
          "성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "SMS 발송 실패",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = {
                  @ExampleObject(name = "잘못된 휴대폰 번호",
                      value = "{ \"code\": \"SM-400-01\", \"message\": \"잘못된 핸드폰 번호 형식 입니다.\" }"),
                  @ExampleObject(name = "재요청 너무 빠름",
                      value =
                          "{ \"code\": \"SM-400-02\", \"message\": \"너무 빠르게 요청하셨습니다. (1분 후)잠시 후 "
                              + "다시 "
                              + "시도해주세요.\" }"),
                  @ExampleObject(
                      name = "일일 인증 제한 초과",
                      value = "{ \"code\": \"SM-400-03\", \"message\": \"오늘은 더 이상 인증번호를 보낼 수 없습니다"
                          + ". 내일 다시 시도해주세요.\" }",
                      description = "하루 SMS 인증 시도 횟수를 초과한 경우"
                  )
              }
          )
      )
  })
  ApiResponse<SmsAuthResponse> authSend(@RequestBody UserDto.SmsSendRequest request,
      HttpServletRequest httpRequest);

  @Operation(summary = "SMS 인증번호 검증", description = "입력한 인증번호가 유효한지 검증합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description =
          "성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "인증 실패",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = {
                  @ExampleObject(
                      name = "인증번호 미요청",
                      value = "{ \"code\": \"SM-400-05\", \"message\": \"인증번호가 요청되지 않았습니다. 먼저 "
                          + "인증번호를 요청해주세요.\" }",
                      description = "인증번호 요청 없이 검증을 시도한 경우"
                  ),
                  @ExampleObject(
                      name = "인증 실패 횟수 초과",
                      value = "{ \"code\": \"SM-400-04\", \"message\": \"인증 실패 횟수를 초과했습니다. 새로운 "
                          + "인증번호를 다시 요청해주세요.\" }",
                      description = "인증번호를 여러 번(5번) 틀린 경우"
                  ),
                  @ExampleObject(
                      name = "인증번호 불일치",
                      value = "{ \"code\": \"SM-400-06\", \"message\": \"인증번호가 일치하지 않습니다.\" }",
                      description = "입력한 인증번호가 서버에 저장된 인증번호와 일치하지 않은 경우"
                  )
              }
          )
      )
  })
  ApiResponse<Void> verify(@RequestBody SmsVerifyRequest request);

  @SecurityRequirement(name = "refreshToken")
  @Operation(
      summary = "Access Token 재발급 (Refresh Token Rotation)",
      description =
          """
              Refresh Token을 이용해 Access Token을 재발급합니다.
              
              본 API는 **Refresh Token Rotation 방식**을 사용합니다.
              - 요청에 사용된 Refresh Token은 즉시 폐기됩니다.
              - 새로운 Access Token과 새로운 Refresh Token을 함께 발급합니다.
              - 이미 사용되었거나 폐기된 Refresh Token을 다시 사용하면 요청이 거부됩니다.
              
              요청 헤더:
              - refreshToken: Refresh Token 문자열
              """
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "토큰 재발급 성공 (새 Access Token + 새 Refresh Token 반환)"
      ),

      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "401",
          description = "리프레시 토큰 인증 실패",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = BusinessException.class),
              examples = {

                  @ExampleObject(
                      name = "유효하지 않은 Refresh Token",
                      value = """
                          {
                            "code": "JWT-401-01",
                            "message": "유효하지 않은 리프레시 토큰입니다."
                          }
                          """,
                      description =
                          """
                              - refreshToken 헤더가 없거나 빈 값인 경우
                              - 토큰 형식이 올바르지 않은 경우
                              - JWT 서명 검증 실패 또는 변조된 토큰
                              """
                  ),

                  @ExampleObject(
                      name = "만료된 Refresh Token",
                      value = """
                          {
                            "code": "JWT-401-02",
                            "message": "만료된 리프레시 토큰입니다."
                          }
                          """,
                      description =
                          """
                              - Refresh Token의 만료 시간(exp)이 지난 경우
                              """
                  ),

                  @ExampleObject(
                      name = "이미 사용(폐기)된 Refresh Token",
                      value = """
                          {
                            "code": "JWT-401-03",
                            "message": "이미 사용(폐기)된 리프레시 토큰입니다."
                          }
                          """,
                      description =
                          """
                              - 이미 rotation으로 폐기된 Refresh Token을 다시 사용한 경우
                              - Redis에 해당 jti가 존재하지 않는 경우
                              - 중복 요청 또는 재사용 공격으로 판단되는 경우
                              """
                  )
              }
          )
      )
  })
  @PostMapping("/refresh")
  ApiResponse<TokenPair> refresh(
      @Parameter(hidden = true)
      @RequestHeader("refreshToken") String refreshToken
  );
}
