package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudioBoastQueryRepository {

  Page<StudioBoast> findAllRandomly(Pageable pageable);
}
