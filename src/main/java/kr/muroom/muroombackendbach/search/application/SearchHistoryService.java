package kr.muroom.muroombackendbach.search.application;

import java.util.List;
import java.util.Optional;
import kr.muroom.muroombackendbach.common.context.AnonymousUserContext;
import kr.muroom.muroombackendbach.search.domain.entity.RecentSearch;
import kr.muroom.muroombackendbach.search.domain.entity.SearchLog;
import kr.muroom.muroombackendbach.search.domain.repository.RecentSearchRepository;
import kr.muroom.muroombackendbach.search.domain.repository.SearchLogRepository;
import kr.muroom.muroombackendbach.search.presentation.dto.response.RecentSearchResponse;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.user.domain.repository.MusicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SearchHistoryService {

  private static final int MAX_RECENT_SEARCH_COUNT = 7;

  private final RecentSearchRepository recentSearchRepository;
  private final SearchLogRepository searchLogRepository;
  private final MusicianRepository musicianRepository;

  public void addSearchKeyword(Long musicianId, String searchKeyword) {
    if (searchKeyword == null || searchKeyword.isBlank()) {
      return;
    }
    Musician musicianProxy = null;
    if (musicianId != null) {
      musicianProxy = musicianRepository.getReferenceById(musicianId);
    }

    saveSearchLog(musicianProxy, searchKeyword);

    if (musicianId != null) {
      managerRecentSearchForMusician(musicianProxy, searchKeyword);
    }
  }

  public List<RecentSearchResponse> getRecentSearchKeywords(Long musicianId) {
    Musician musicianProxy = musicianRepository.getReferenceById(musicianId);
    return recentSearchRepository.findByMusicianOrderByRecentlySearchedAtAsc(musicianProxy)
        .stream()
        .map(RecentSearchResponse::from)
        .toList();
  }

  private void saveSearchLog(Musician musician, String searchKeyword) {
    SearchLog searchLog;
    if (musician != null) {
      searchLog = SearchLog.byMusician(musician, searchKeyword);
    } else {
      String anonymousUserId = AnonymousUserContext.getAnonymousUserId();
      searchLog = SearchLog.byAnonymousUser(anonymousUserId, searchKeyword);
    }
    searchLogRepository.save(searchLog);
  }

  private void managerRecentSearchForMusician(Musician musician, String searchKeyword) {
    Optional<RecentSearch> existingRecentSearch = recentSearchRepository
        .findByMusicianAndKeyword(musician, searchKeyword);

    if (existingRecentSearch.isPresent()) {
      RecentSearch recentSearch = existingRecentSearch.get();
      recentSearch.updateRecentlySearchedAt();
    } else {
      RecentSearch newRecentSearch = RecentSearch.builder()
          .musician(musician)
          .keyword(searchKeyword)
          .build();
      recentSearchRepository.save(newRecentSearch);
      List<RecentSearch> recentSearches =
          recentSearchRepository.findByMusicianOrderByRecentlySearchedAtAsc(musician);

      if (recentSearches.size() > MAX_RECENT_SEARCH_COUNT) {
        recentSearchRepository.deleteAll(
            recentSearches.subList(0, recentSearches.size() - MAX_RECENT_SEARCH_COUNT));
      }
    }
  }
}
