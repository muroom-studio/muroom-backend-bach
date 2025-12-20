package kr.muroom.muroombackendbach.faq.domain.repository;

import kr.muroom.muroombackendbach.faq.domain.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, Long> {

}
