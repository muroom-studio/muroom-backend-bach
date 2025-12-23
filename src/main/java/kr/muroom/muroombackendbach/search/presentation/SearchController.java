package kr.muroom.muroombackendbach.search.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.search.application.SearchHistoryService;
import kr.muroom.muroombackendbach.search.presentation.dto.response.RecentSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/search-histories")
public class SearchController {

  private final SearchHistoryService searchHistoryService;

  @Operation(summary = "회원의 최근 검색어 조회", description = "로그인한 사용자의 최근 검색어를 최대 7개까지 조회합니다.")
  @SecurityRequirement(name = "Authorization")
  @GetMapping("/recent")
  @PreAuthorize("isAuthenticated()")
  public ApiResponse<List<RecentSearchResponse>> getRecentSearchKeywords(
      @AuthenticationPrincipal Long musicianId) {
    if (musicianId == null) {
      return ApiResponse.success(List.of());
    }

    List<RecentSearchResponse> response = searchHistoryService.getRecentSearchKeywords(musicianId);

    return ApiResponse.success(response);
  }
}
