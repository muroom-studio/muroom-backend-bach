package kr.muroom.muroombackendbach.search.presentation.dto.response;

import kr.muroom.muroombackendbach.search.domain.entity.RecentSearch;
import lombok.Builder;

@Builder
public record RecentSearchResponse(
    String searchKeyword
) {

  public static RecentSearchResponse from(RecentSearch recentSearch) {
    return RecentSearchResponse.builder()
        .searchKeyword(recentSearch.getKeyword())
        .build();
  }
}
