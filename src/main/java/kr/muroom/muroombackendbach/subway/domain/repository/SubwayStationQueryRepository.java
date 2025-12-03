package kr.muroom.muroombackendbach.subway.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.subway.presentation.dto.StationWithDistance;
import org.locationtech.jts.geom.Point;

public interface SubwayStationQueryRepository {

  List<StationWithDistance> findNearbyStationsWithDistance(Point point, int radius);
}
