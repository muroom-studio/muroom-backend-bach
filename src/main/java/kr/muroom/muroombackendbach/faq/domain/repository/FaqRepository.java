package kr.muroom.muroombackendbach.faq.domain.repository;

import kr.muroom.muroombackendbach.faq.domain.entity.Faq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, Long> {

  @Override
  @EntityGraph(attributePaths = {"category"})
  Page<Faq> findAll(Pageable pageable);
}
