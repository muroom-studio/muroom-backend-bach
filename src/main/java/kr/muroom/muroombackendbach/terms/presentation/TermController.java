package kr.muroom.muroombackendbach.terms.presentation;

import static kr.muroom.muroombackendbach.terms.presentation.dto.TermDto.*;

import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.terms.application.TermService;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

  @GetMapping("/owner")
  public ApiResponse<List<TermsWithContentDto>> getOwnerTerms(
      @RequestParam(required = false) List<TermsType> types) {
    return ApiResponse.success(termService.getTermsOwnerByType(types));
  }

}
