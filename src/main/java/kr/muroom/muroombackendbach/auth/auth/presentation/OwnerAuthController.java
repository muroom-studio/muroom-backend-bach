package kr.muroom.muroombackendbach.auth.auth.presentation;

import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.auth.application.OwnerPasswordLoginService;
import kr.muroom.muroombackendbach.auth.auth.presentation.docs.OwnerAuthControllerDocs;
import kr.muroom.muroombackendbach.auth.auth.presentation.dto.request.OwnerLoginRequest;
import kr.muroom.muroombackendbach.auth.auth.presentation.dto.response.OwnerLoginResponse;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/owner")
@RequiredArgsConstructor
@Slf4j
public class OwnerAuthController implements OwnerAuthControllerDocs {

  private final OwnerPasswordLoginService ownerPasswordLoginService;

  @PostMapping("/login")
  public ApiResponse<OwnerLoginResponse> login(
      @RequestBody @Valid OwnerLoginRequest request) {
    return ApiResponse.success(ownerPasswordLoginService.login(request));
  }
}
