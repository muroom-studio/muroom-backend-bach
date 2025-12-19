package kr.muroom.muroombackendbach.user.presentation;

import static kr.muroom.muroombackendbach.user.presentation.dto.OwnerDto.*;

import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.user.application.OwnerService;
import kr.muroom.muroombackendbach.user.presentation.dto.request.OwnerSignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/owner")
@RequiredArgsConstructor
public class OwnerController {

  private final OwnerService ownerService;

  @GetMapping("/check-email")
  public ApiResponse<EmailCheckResponse> checkEmail(@Valid EmailCheckRequest request) {
    return ApiResponse.success(ownerService.checkEmail(request.email()));
  }

  @PostMapping("/register")
  public ApiResponse<Long> registerOwner(@RequestBody OwnerSignupRequest request) {
    return ApiResponse.created(ownerService.registerOwner(request));
  }

  @PostMapping("/login")
  public ApiResponse<Void> login(@Valid @RequestBody OwnerLoginRequest request) {
    return ApiResponse.success();
  }

}
