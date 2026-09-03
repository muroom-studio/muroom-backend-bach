package kr.muroom.muroombackendbach.studioboasting.domain.repository;

import static kr.muroom.muroombackendbach.studioboasting.domain.entity.QStudioBoast.studioBoast;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.studioboasting.domain.entity.StudioBoast;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class StudioBoastRepositoryImpl implements StudioBoastQueryRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<StudioBoast> findAllRandomly(Pageable pageable) {
    Long countResult = queryFactory.select(studioBoast.count()).from(studioBoast).fetchOne();
    long totalCount = (countResult != null) ? countResult : 0L;
    if (totalCount == 0) {
      return Page.empty(pageable);
    }
    int pageSize = pageable.getPageSize();
    // 랜덤 오프셋 계산 (전체 개수 내에서 무작위 시작점)
    long randomOffset = (long) (Math.random() * totalCount);
    // 1. 랜덤 오프셋부터 페이지 사이즈만큼 조회
    List<StudioBoast> results = queryFactory
        .selectFrom(studioBoast)
        .offset(randomOffset)
        .limit(pageSize)
        .fetch();
    // 2. 만약 결과가 페이지 사이즈보다 적다면, 처음부터 부족한 만큼 다시 조회
    if (results.size() < pageSize) {
      long remaining = pageSize - results.size();
      List<StudioBoast> additionalResults = queryFactory
          .selectFrom(studioBoast)
          .offset(0)
          .limit(remaining)
          .fetch();
      results.addAll(additionalResults);
    }
    // 중복 제거 (randomOffset과 0부터 시작하는 쿼리에서 중복이 발생할 수 있음)
    results = results.stream().distinct().limit(pageSize).collect(Collectors.toList());
    // PageImpl 생성 시 totalCount는 전체 개수를 사용
    return new PageImpl<>(results, pageable, totalCount);
  }
}
