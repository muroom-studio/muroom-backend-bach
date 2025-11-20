package kr.muroom.muroombackendbach.beta.registration.domain.repository;

import kr.muroom.muroombackendbach.beta.registration.domain.entity.BetaRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BetaRegistrationQueryRepository {
  Page<BetaRegistration> findAllWithImages(Pageable pageable);
}
