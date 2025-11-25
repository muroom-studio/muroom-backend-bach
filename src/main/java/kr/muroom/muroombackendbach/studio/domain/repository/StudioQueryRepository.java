package kr.muroom.muroombackendbach.studio.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudioQueryRepository {

  List<Studio> findStudiosWithinBounds(
      Double minLatitude, Double maxLatitude,
      Double minLongitude, Double maxLongitude
  );

  Page<Studio> findStudiosForMapList(
      Double minLatitude, Double maxLatitude,
      Double minLongitude, Double maxLongitude,
      Pageable pageable
  );
}
