package kr.muroom.muroombackendbach.terms.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.muroom.muroombackendbach.terms.domain.entity.QTerm;
import kr.muroom.muroombackendbach.terms.domain.entity.QTermContent;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import kr.muroom.muroombackendbach.terms.presentation.dto.TermDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TermQueryRepositoryImpl implements TermQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<TermDto.TermsWithContentDto> findLatestTermsByRoleAndTypes(
      String role,
      List<TermsType> types
  ) {
    QTerm t = QTerm.term;
    QTerm t2 = new QTerm("t2");

    return queryFactory
        .select(Projections.constructor(
            TermDto.TermsWithContentDto.class,
            t.id,
            t.code,
            t.targetRole,
            t.version,
            t.isMandatory,
            t.effectiveAt
        ))
        .from(t)
        .where(
            t.targetRole.eq(role),
            t.code.in(types),
            t.effectiveAt.eq(
                JPAExpressions
                    .select(t2.effectiveAt.max())
                    .from(t2)
                    .where(
                        t2.targetRole.eq(t.targetRole),
                        t2.code.eq(t.code)
                    )
            )
        )
        .fetch();
  }
}
