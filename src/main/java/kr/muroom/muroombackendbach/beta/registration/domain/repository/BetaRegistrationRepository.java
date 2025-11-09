package kr.muroom.muroombackendbach.beta.registration.domain.repository;

import kr.muroom.muroombackendbach.beta.registration.domain.entity.BetaRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetaRegistrationRepository extends JpaRepository<BetaRegistration, Long>,
    BetaRegistrationQueryRepository {
  
}
