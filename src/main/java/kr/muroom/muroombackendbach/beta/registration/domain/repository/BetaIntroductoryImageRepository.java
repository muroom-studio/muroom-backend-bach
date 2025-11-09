package kr.muroom.muroombackendbach.beta.registration.domain.repository;

import kr.muroom.muroombackendbach.beta.registration.domain.entity.BetaIntroductoryImage;
import org.springframework.data.jpa.repository.JpaRepository;

@SuppressWarnings("unused")
public interface BetaIntroductoryImageRepository extends
    JpaRepository<BetaIntroductoryImage, Long> {

}
