package kr.muroom.muroombackendbach.studio.domain.repository;

import kr.muroom.muroombackendbach.studio.domain.entity.Studio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudioRepository extends JpaRepository<Studio, Long>, StudioQueryRepository {

}
