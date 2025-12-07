package kr.muroom.muroombackendbach.terms.domain.repository;

import static kr.muroom.muroombackendbach.terms.presentation.dto.TermDto.*;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.muroom.muroombackendbach.terms.domain.entity.QTerm;
import kr.muroom.muroombackendbach.terms.domain.entity.TargetRole;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TermQueryRepositoryImpl implements TermQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<TermsWithContentDto> findLatestTermsByRoleAndTypes(
      TargetRole role,
      List<TermsType> types
  ) {
    QTerm t = QTerm.term;
    QTerm t2 = new QTerm("t2");

    NumberExpression<Integer> major =
        Expressions.numberTemplate(Integer.class, "cast(split_part({0}, '.', 1) as integer)",
            t.version);
    NumberExpression<Integer> minor =
        Expressions.numberTemplate(Integer.class, "cast(split_part({0}, '.', 2) as integer)",
            t.version);
    NumberExpression<Integer> patch =
        Expressions.numberTemplate(Integer.class, "cast(split_part({0}, '.', 3) as integer)",
            t.version);

    NumberExpression<Integer> major2 =
        Expressions.numberTemplate(Integer.class, "cast(split_part({0}, '.', 1) as integer)",
            t2.version);
    NumberExpression<Integer> minor2 =
        Expressions.numberTemplate(Integer.class, "cast(split_part({0}, '.', 2) as integer)",
            t2.version);
    NumberExpression<Integer> patch2 =
        Expressions.numberTemplate(Integer.class, "cast(split_part({0}, '.', 3) as integer)",
            t2.version);

    return queryFactory
        .select(Projections.constructor(
            TermsWithContentDto.class,
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
            major.eq(
                JPAExpressions.select(major2.max())
                    .from(t2)
                    .where(
                        t2.targetRole.eq(t.targetRole),
                        t2.code.eq(t.code)
                    )
            ),
            minor.eq(
                JPAExpressions.select(minor2.max())
                    .from(t2)
                    .where(
                        t2.targetRole.eq(t.targetRole),
                        t2.code.eq(t.code),
                        major2.eq(major) // 같은 major 내
                    )
            ),
            patch.eq(
                JPAExpressions.select(patch2.max())
                    .from(t2)
                    .where(
                        t2.targetRole.eq(t.targetRole),
                        t2.code.eq(t.code),
                        major2.eq(major),
                        minor2.eq(minor)
                    )
            ),

            // 2) 같은 version 내에서는 effectiveAt 최신
            t.effectiveAt.eq(
                JPAExpressions
                    .select(t2.effectiveAt.max())
                    .from(t2)
                    .where(
                        t2.targetRole.eq(t.targetRole),
                        t2.code.eq(t.code),
                        t2.version.eq(t.version)
                    )
            ),

            t.id.eq(
                JPAExpressions
                    .select(t2.id.max())
                    .from(t2)
                    .where(
                        t2.targetRole.eq(t.targetRole),
                        t2.code.eq(t.code),
                        t2.version.eq(t.version),
                        t2.effectiveAt.eq(t.effectiveAt)
                    )
            )
        )
        .fetch();
  }

}
