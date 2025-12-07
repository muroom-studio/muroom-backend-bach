package kr.muroom.muroombackendbach.user.presentation;

import kr.muroom.muroombackendbach.auth.login.SmsVerificationService;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.user.application.UserService;
import kr.muroom.muroombackendbach.user.presentation.dto.UserDto;
import kr.muroom.muroombackendbach.user.presentation.dto.UserDto.SmsSendRequest;
import kr.muroom.muroombackendbach.user.presentation.dto.UserDto.VerifyRequest;
import kr.muroom.muroombackendbach.user.presentation.dto.UserDto.VerifyResponse;
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
public class UserController {

  private final UserService userService;
  private final SmsVerificationService smsVerificationService;

  @GetMapping("/nickname/check")
  public ApiResponse<UserDto.NicknameCheckResponse> checkNickname(@RequestParam String nickname) {
    boolean available = userService.isNicknameAvailable(nickname);
    return ApiResponse.success(new UserDto.NicknameCheckResponse(available));
  }

  @PostMapping("/sms/auth")
  public ApiResponse<Void> authSend(@Validated @RequestBody SmsSendRequest request) {
    smsVerificationService.sendVerificationCode(request.phone());
    return ApiResponse.success();
  }

  @PostMapping("/sms/verify")
  public ApiResponse<VerifyResponse> verify(@Validated @RequestBody VerifyRequest request) {
    return ApiResponse.success(
        smsVerificationService.verifyCode(request.phoneNumber(), request.code()));
  }
}
