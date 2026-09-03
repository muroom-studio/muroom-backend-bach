package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioBoastRepository extends JpaRepository<StudioBoast, Long>,
    StudioBoastQueryRepository {

  Page<StudioBoast> findAllByCreatorUserId(Long creatorUserId, Pageable pageable);

  boolean existsByCreatorUserId(Long creatorUserId);
}
