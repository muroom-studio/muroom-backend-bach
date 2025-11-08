package kr.muroom.muroombackendbach.beta.registration.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.beta.registration.domain.entity.BetaRegistration;

public interface BetaRegistrationQueryRepository {

  List<BetaRegistration> findAllWithImages();
}
