package kr.muroom.muroombackendbach.terms.domain.repository;

import kr.muroom.muroombackendbach.terms.domain.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Terms, Long>, TermQueryRepository{
}
