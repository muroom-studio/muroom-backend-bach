package kr.muroom.muroombackendbach.terms.domain.repository;

import kr.muroom.muroombackendbach.terms.domain.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long>, TermQueryRepository{
}
