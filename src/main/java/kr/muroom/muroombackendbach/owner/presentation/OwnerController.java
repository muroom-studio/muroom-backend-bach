package kr.muroom.muroombackendbach.owner.presentation;

import jakarta.validation.Valid;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.musician.presentation.dto.request.MusicianSignupRequest;
import kr.muroom.muroombackendbach.musician.presentation.dto.response.MusicianSignupResponse;
import kr.muroom.muroombackendbach.owner.application.OwnerService;
import kr.muroom.muroombackendbach.owner.presentation.dto.request.OwnerSignupRequest;
import kr.muroom.muroombackendbach.owner.presentation.dto.response.OwnerSignupResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/owners")
@RequiredArgsConstructor
@Slf4j
public class OwnerController {

  private final OwnerService ownerService;

  @PostMapping("/register")
  public ApiResponse<OwnerSignupResponse> registerOwner(
      @Valid @RequestBody OwnerSignupRequest request
  ) {
    OwnerSignupResponse response = ownerService.registerOwner(request);
    return ApiResponse.created(response);
  }
}
