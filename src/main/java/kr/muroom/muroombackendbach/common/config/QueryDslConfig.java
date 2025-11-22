package kr.muroom.muroombackendbach.common.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDsl 설정 클래스입니다.
 */
@Configuration
@RequiredArgsConstructor
public class QueryDslConfig {

  private final EntityManager entityManager;

  /**
   * JPAQueryFactory 빈을 생성합니다.
   *
   * @return JPAQueryFactory 인스턴스
   */
  @Bean
  public JPAQueryFactory jpaQueryFactory() {
    return new JPAQueryFactory(entityManager);
  }
}