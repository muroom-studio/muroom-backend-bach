package kr.muroom.muroombackendbach.sms.presentation.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.sms.presentation.dto.SmsAuthResponse;
import kr.muroom.muroombackendbach.sms.presentation.dto.request.MusicianSmsSendRequest;
import kr.muroom.muroombackendbach.sms.presentation.dto.request.MusicianSmsVerifyRequest;
import kr.muroom.muroombackendbach.sms.presentation.dto.response.SmsVerifyResponse;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Sms - Sms API")
public interface SmsControllerDocs {

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
  ApiResponse<SmsAuthResponse> authSend(@RequestBody MusicianSmsSendRequest request,
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
  ApiResponse<SmsVerifyResponse> verify(@RequestBody MusicianSmsVerifyRequest request);

}
