package kr.muroom.muroombackendbach.search.domain.repository;

import java.util.List;
import java.util.Optional;
import kr.muroom.muroombackendbach.search.domain.entity.RecentSearch;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import org.springframework.data.repository.CrudRepository;

public interface RecentSearchRepository extends CrudRepository<RecentSearch, Long> {

  Optional<RecentSearch> findByMusicianAndKeyword(Musician musician, String searchKeyword);

  List<RecentSearch> findByMusicianOrderByRecentlySearchedAtAsc(Musician musician);
}
