package kr.muroom.muroombackendbach.terms.domain.repository;

import kr.muroom.muroombackendbach.terms.domain.entity.OwnerAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OwnerAgreementRepository extends JpaRepository<OwnerAgreement, Long> {
}
