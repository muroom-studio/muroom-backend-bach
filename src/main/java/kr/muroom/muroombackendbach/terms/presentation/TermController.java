package kr.muroom.muroombackendbach.terms.presentation;

import static kr.muroom.muroombackendbach.terms.presentation.dto.TermDto.*;

import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.terms.application.TermService;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import kr.muroom.muroombackendbach.terms.presentation.dto.TermRegisterRequest;
import kr.muroom.muroombackendbach.terms.presentation.dto.TermUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermController {

  private final TermService termService;

  @GetMapping("/musician")
  public ApiResponse<List<TermsWithContentDto>> getMusicianTerms(
      @RequestParam List<TermsType> types) {
    return ApiResponse.success(termService.getTermsMusicianByType(types));
  }

  @GetMapping("/{termId}")
  public ApiResponse<TermContentDto> getTermById(@PathVariable Long termId) {
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

  @GetMapping("/owner")
  public ApiResponse<List<TermsWithContentDto>> getOwnerTerms(
      @RequestParam List<TermsType> types) {
    return ApiResponse.success(termService.getTermsOwnerByType(types));
  }

}
