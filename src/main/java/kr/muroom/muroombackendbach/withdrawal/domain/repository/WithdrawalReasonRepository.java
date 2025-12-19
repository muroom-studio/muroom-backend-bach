package kr.muroom.muroombackendbach.withdrawal.domain.repository;

import kr.muroom.muroombackendbach.withdrawal.domain.entity.WithdrawalReason;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalReasonRepository extends JpaRepository<WithdrawalReason, Long> {

  boolean existsByCode(String code);
  
}
