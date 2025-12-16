package kr.muroom.muroombackendbach.withdrawal.domain.repository;

import kr.muroom.muroombackendbach.withdrawal.domain.entity.MusicianWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MusicianWithdrawalRepository extends JpaRepository<MusicianWithdrawal, Long> {

}
