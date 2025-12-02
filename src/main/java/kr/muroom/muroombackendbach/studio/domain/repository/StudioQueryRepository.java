package kr.muroom.muroombackendbach.studio.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import kr.muroom.muroombackendbach.studio.presentation.dto.request.MapSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StudioQueryRepository {

  List<Studio> findStudiosWithinBounds(MapSearchRequest request);

  Page<Studio> findStudiosForMapList(MapSearchRequest request, Pageable pageable);

}
