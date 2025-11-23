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
    public List<TermDto.TermsWithContentDto> findLatestTermsByRoleAndTypes(String role, List<TermsType> types) {
        QTerm t = QTerm.term;
        QTerm t2 = new QTerm("t2");
        QTermContent tc = QTermContent.termContent;

        return queryFactory
                .select(Projections.constructor(
                        TermDto.TermsWithContentDto.class,
                        t.id,
                        t.type,
                        t.targetRole,
                        t.version,
                        t.isMandatory,
                        t.effectiveAt,
                        tc.content
                ))
                .from(t)
                .join(tc).on(tc.term.eq(t))
            .where(
                t.targetRole.eq(role),
                t.type.in(types),
                t.effectiveAt.eq(JPAExpressions
                    .select(t2.effectiveAt.max())
                    .from(t2)
                    .where(t2.targetRole.eq(t.targetRole), t2.type.eq(t.type))
                )
            )
            .fetch();
    }
}
