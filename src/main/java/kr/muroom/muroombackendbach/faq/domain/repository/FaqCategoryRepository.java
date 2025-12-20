package kr.muroom.muroombackendbach.faq.domain.repository;

import kr.muroom.muroombackendbach.faq.domain.entity.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqCategoryRepository extends JpaRepository<FaqCategory, Long> {

}
