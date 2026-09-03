package kr.muroom.muroombackendbach.auth.auth.presentation;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.auth.application.OwnerPasswordLoginService;
import kr.muroom.muroombackendbach.auth.auth.domain.entity.UserType;
import kr.muroom.muroombackendbach.auth.auth.presentation.docs.OwnerAuthControllerDocs;
import kr.muroom.muroombackendbach.auth.auth.presentation.dto.request.OwnerLoginRequest;
import kr.muroom.muroombackendbach.auth.auth.presentation.dto.response.OwnerLoginResponse;
import kr.muroom.muroombackendbach.auth.session.SessionAuthService;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
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
  private final SessionAuthService sessionAuthService;

  @PostMapping("/login")
  public ApiResponse<OwnerLoginResponse> login(
      @RequestBody @Valid OwnerLoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    OwnerLoginResponse response = ownerPasswordLoginService.login(request);

    sessionAuthService.login(httpRequest, httpResponse, UserType.OWNER,
        Long.valueOf(response.ownerId()));

    return ApiResponse.success(response);
  }

  @PreAuthorize("hasRole('OWNER')")
  @PostMapping("/logout")
  public ApiResponse<Void> logout(
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
  ) {
    sessionAuthService.logout(httpRequest, httpResponse);
    return ApiResponse.success();
  }
}
