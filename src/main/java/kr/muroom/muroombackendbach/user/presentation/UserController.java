package kr.muroom.muroombackendbach.user.presentation;

import static kr.muroom.muroombackendbach.user.presentation.dto.UserDto.*;

import kr.muroom.muroombackendbach.common.sms.application.SmsVerificationService;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.sms.presentation.dto.SmsAuthResponse;
import kr.muroom.muroombackendbach.user.application.UserService;
import kr.muroom.muroombackendbach.user.presentation.docs.UserControllerDocs;
import kr.muroom.muroombackendbach.user.presentation.dto.UserDto.SmsSendRequest;
import kr.muroom.muroombackendbach.user.presentation.dto.UserDto.SmsVerifyRequest;
import kr.muroom.muroombackendbach.user.presentation.dto.UserDto.SmsVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController implements UserControllerDocs {

  private final UserService userService;
  private final SmsVerificationService smsVerificationService;

  @GetMapping("/nickname/check")
  public ApiResponse<NicknameCheckResponse> checkNickname(@RequestParam String nickname) {
    boolean available = userService.isNicknameAvailable(nickname);
    return ApiResponse.success(new NicknameCheckResponse(available));
  }

  @PostMapping("/sms/auth")
  public ApiResponse<SmsAuthResponse> authSend(@Validated @RequestBody SmsSendRequest request) {
    return ApiResponse.success(smsVerificationService.sendVerificationCode(request.phone()));
  }

  @PostMapping("/sms/verify")
  public ApiResponse<Void> verify(@Validated @RequestBody SmsVerifyRequest request) {
    smsVerificationService.verifyCode(request.phone(), request.code());
    return ApiResponse.success();
  }
}
