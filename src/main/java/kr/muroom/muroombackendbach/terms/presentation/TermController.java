package kr.muroom.muroombackendbach.terms.presentation;

import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.terms.application.TermService;
import kr.muroom.muroombackendbach.terms.presentation.dto.request.TermRegisterRequest;
import kr.muroom.muroombackendbach.terms.presentation.dto.request.TermUpdateRequest;
import kr.muroom.muroombackendbach.terms.presentation.dto.response.TermDetailResponse;
import kr.muroom.muroombackendbach.terms.presentation.dto.response.TermSimpleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermController implements TermControllerDocs {

  private final TermService termService;

  @GetMapping("/musician/signup")
  public ApiResponse<List<TermDetailResponse>> getMusicianTerms() {
    return ApiResponse.success(termService.getTermsMusicianByType());
  }

  @GetMapping("/owner/signup")
  public ApiResponse<List<TermDetailResponse>> getOwnerTerms() {
    return ApiResponse.success(termService.getTermsOwnerByType());
  }

  @GetMapping("/{termId}")
  public ApiResponse<TermSimpleResponse> getTermById(@PathVariable Long termId) {
    return ApiResponse.success(termService.getTermContent(termId));
  }

  @PostMapping
  public ApiResponse<Void> registerMusicianTerms(
      @Validated @RequestBody TermRegisterRequest request) {
    termService.registerMusicianTerms(request);
    return ApiResponse.success();
  }

  @PutMapping("/{termId}")
  public ApiResponse<Void> updateMusicianTerms(@PathVariable Long termId,
      @RequestBody TermUpdateRequest request) {
    termService.updateMusicianTerms(termId, request);
    return ApiResponse.success();
  }
}
