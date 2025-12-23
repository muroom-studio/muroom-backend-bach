package kr.muroom.muroombackendbach.inquiry.domain.repository;

import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryCategoryRepository extends JpaRepository<InquiryCategory, Long> {

}
