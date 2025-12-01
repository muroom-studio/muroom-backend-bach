package kr.muroom.muroombackendbach.search.presentation.dto.response;

import kr.muroom.muroombackendbach.search.domain.entity.RecentSearch;
import lombok.Builder;

@Builder
public record RecentSearchResponse(
    Long musicianId,
    String searchKeyword
) {

  public static RecentSearchResponse from(RecentSearch recentSearch) {
    return RecentSearchResponse.builder()
        .musicianId(recentSearch.getMusician().getId())
        .searchKeyword(recentSearch.getKeyword())
        .build();
  }
}
