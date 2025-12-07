package kr.muroom.muroombackendbach.terms.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.terms.domain.entity.TargetRole;
import kr.muroom.muroombackendbach.terms.domain.entity.Term;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long>, TermQueryRepository {

  List<Term> findByCodeAndTargetRoleOrderByVersionAsc(TermsType code, TargetRole targetRole);
}
