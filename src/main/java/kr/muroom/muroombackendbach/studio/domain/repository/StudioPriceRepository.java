package kr.muroom.muroombackendbach.studio.domain.repository;

import java.util.Collection;
import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioPriceRepository extends JpaRepository<StudioPrice, Long> {

  List<StudioPrice> findAllByStudioIdIn(Collection<Long> studioIds);
}
