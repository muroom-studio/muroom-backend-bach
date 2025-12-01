package kr.muroom.muroombackendbach.search.domain.repository;

import kr.muroom.muroombackendbach.search.domain.entity.SearchLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchLogRepository extends JpaRepository<SearchLog, Long> {

}
