package kr.muroom.muroombackendbach.admin.owner.presentation;

import kr.muroom.muroombackendbach.admin.owner.application.AdminOwnerService;
import kr.muroom.muroombackendbach.admin.owner.presentation.request.OwnerCreateRequest;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/owners")
public class AdminOwnerController {

  private final AdminOwnerService adminOwnerService;

  @GetMapping("/generate-nickname")
  public ApiResponse<String> generateOwnerNickname() {
    String response = adminOwnerService.generateOwnerUniqueNickname();
    return ApiResponse.success(response);
  }

  @PostMapping
  public ApiResponse<Void> createOwner(@Validated @RequestBody OwnerCreateRequest request) {
    adminOwnerService.createOwner(request);
    return ApiResponse.success();
  }
}
