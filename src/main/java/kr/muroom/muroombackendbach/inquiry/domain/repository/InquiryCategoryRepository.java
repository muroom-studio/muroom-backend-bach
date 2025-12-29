package kr.muroom.muroombackendbach.inquiry.domain.repository;

import java.util.Collection;
import java.util.List;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryCategoryRepository extends JpaRepository<InquiryCategory, Long> {

  List<InquiryCategory> findAllByOrderBySequenceOrderAscIdAsc();
}
