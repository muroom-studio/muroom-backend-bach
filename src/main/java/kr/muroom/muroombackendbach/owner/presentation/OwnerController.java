package kr.muroom.muroombackendbach.owner.presentation;

import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.config.CurrentUserId;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.owner.application.OwnerService;
import kr.muroom.muroombackendbach.owner.presentation.docs.OwnerControllerDocs;
import kr.muroom.muroombackendbach.owner.presentation.dto.request.OwnerSignupRequest;
import kr.muroom.muroombackendbach.owner.presentation.dto.request.UpdateOwnerProfileRequest;
import kr.muroom.muroombackendbach.owner.presentation.dto.response.OwnerProfileResponse;
import kr.muroom.muroombackendbach.owner.presentation.dto.response.OwnerSignupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/owners")
@RequiredArgsConstructor
@Slf4j
public class OwnerController implements OwnerControllerDocs {

  private final OwnerService ownerService;

  @PostMapping("/register")
  public ApiResponse<OwnerSignupResponse> registerOwner(
      @Valid @RequestBody OwnerSignupRequest request
  ) {
    OwnerSignupResponse response = ownerService.registerOwner(request);
    return ApiResponse.created(response);
  }

  @GetMapping("/nickname/check")
  public ApiResponse<Void> checkNickname(@RequestParam String nickname) {
    ownerService.isNicknameAvailable(nickname);
    return ApiResponse.success();
  }

  @GetMapping("/phone/check")
  public ApiResponse<Void> checkPhone(@RequestParam String phone) {
    ownerService.isPhoneAvailable(phone);
    return ApiResponse.success();
  }

  @PreAuthorize("hasRole('OWNER')")
  @GetMapping("/me/detail")
  public ApiResponse<OwnerProfileResponse> getMyProfile(@CurrentUserId Long ownerId) {
    return ApiResponse.success(ownerService.getMyProfile(ownerId));
  }

  @PreAuthorize("hasRole('OWNER')")
  @PostMapping("/me/detail")
  public ApiResponse<Void> updateMyProfile(
      @CurrentUserId Long ownerId,
      @RequestBody UpdateOwnerProfileRequest request
  ) {
    ownerService.updateMyProfile(ownerId, request);
    return ApiResponse.success();
  }
}
