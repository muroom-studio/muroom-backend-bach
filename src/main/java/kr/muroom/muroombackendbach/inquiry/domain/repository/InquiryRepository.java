package kr.muroom.muroombackendbach.inquiry.domain.repository;

import java.util.Collection;
import java.util.List;
import kr.muroom.muroombackendbach.inquiry.domain.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

  List<Inquiry> findAllByMusicianIdOrderByCreatedAtDesc(Long musicianId);
}
