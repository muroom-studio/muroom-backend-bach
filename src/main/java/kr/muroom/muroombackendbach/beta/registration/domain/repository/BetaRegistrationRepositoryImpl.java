package kr.muroom.muroombackendbach.beta.registration.domain.repository;

import static kr.muroom.muroombackendbach.beta.registration.domain.entity.QBetaIntroductoryImage.betaIntroductoryImage;
import static kr.muroom.muroombackendbach.beta.registration.domain.entity.QBetaRegistration.betaRegistration;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.muroom.muroombackendbach.beta.registration.domain.entity.BetaRegistration;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@SuppressWarnings({"unused", "ClassCanBeRecord"})
public class BetaRegistrationRepositoryImpl implements BetaRegistrationQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<BetaRegistration> findAllWithImages() {
    return queryFactory
        .selectFrom(betaRegistration).distinct()
        .leftJoin(betaRegistration.introductoryImages, betaIntroductoryImage).fetchJoin()
        .orderBy(betaRegistration.createdAt.desc())
        .fetch();
  }
}
