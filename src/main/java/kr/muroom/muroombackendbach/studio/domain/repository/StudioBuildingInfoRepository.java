package kr.muroom.muroombackendbach.studio.domain.repository;

import kr.muroom.muroombackendbach.studio.domain.entity.StudioBuildingInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioBuildingInfoRepository extends JpaRepository<StudioBuildingInfo, Long> {
}
