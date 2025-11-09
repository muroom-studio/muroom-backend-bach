package kr.muroom.muroombackendbach.beta.inquiry.domain.repository;

import kr.muroom.muroombackendbach.beta.inquiry.domain.entity.BetaInquiry;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BetaInquiryRepository extends JpaRepository<BetaInquiry, Long> {
  
  Page<BetaInquiry> findAll(@NonNull Pageable pageable);
}
