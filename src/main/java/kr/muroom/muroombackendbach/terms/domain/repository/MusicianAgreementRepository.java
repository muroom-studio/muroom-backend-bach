package kr.muroom.muroombackendbach.terms.domain.repository;

import kr.muroom.muroombackendbach.terms.domain.entity.MusicianAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicianAgreementRepository extends JpaRepository<MusicianAgreement, Long> {
}
