package kr.muroom.muroombackendbach.owner.presentation;

import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.auth.annotation.CurrentUserId;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.owner.application.command.OwnerCommandService;
import kr.muroom.muroombackendbach.owner.application.query.OwnerQueryService;
import kr.muroom.muroombackendbach.owner.presentation.docs.OwnerControllerDocs;
import kr.muroom.muroombackendbach.owner.presentation.dto.request.OwnerSignupRequest;
import kr.muroom.muroombackendbach.owner.presentation.dto.request.UpdateOwnerProfileRequest;
import kr.muroom.muroombackendbach.owner.presentation.dto.response.OwnerProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

  private final OwnerCommandService ownerCommandService;
  private final OwnerQueryService ownerQueryService;

  @PostMapping("/register")
  public ApiResponse<Long> registerOwner(
      @Valid @RequestBody OwnerSignupRequest request
  ) {
    Long ownerId = ownerCommandService.registerOwner(request);
    return ApiResponse.created(ownerId);
  }

  @GetMapping("/nickname/check")
  public ApiResponse<Void> checkNickname(@RequestParam String nickname) {
    ownerQueryService.isNicknameAvailable(nickname);
    return ApiResponse.success();
  }

  @GetMapping("/phone/check")
  public ApiResponse<Void> checkPhone(@RequestParam String phone) {
    ownerQueryService.isPhoneAvailable(phone);
    return ApiResponse.success();
  }

  @PreAuthorize("hasRole('OWNER')")
  @GetMapping("/me/detail")
  public ApiResponse<OwnerProfileResponse> getMyProfile(@CurrentUserId Long ownerId) {
    return ApiResponse.success(ownerQueryService.getMyProfile(ownerId));
  }

  @PreAuthorize("hasRole('OWNER')")
  @PatchMapping("/me/detail")
  public ApiResponse<Void> updateMyProfile(
      @CurrentUserId Long ownerId,
      @RequestBody UpdateOwnerProfileRequest request
  ) {
    ownerCommandService.updateMyProfile(ownerId, request);
    return ApiResponse.success();
  }
}
