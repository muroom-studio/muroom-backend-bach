package kr.muroom.muroombackendbach.common.config;

import java.util.List;
import kr.muroom.muroombackendbach.auth.resolver.CurrentUserIdArgumentResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 관련 설정 클래스입니다.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final String[] allowedOrigins;
  private final CurrentUserIdArgumentResolver currentUserIdArgumentResolver;

  public WebConfig(@Value("${cors.allowed-origins}") String[] allowedOrigins,
      CurrentUserIdArgumentResolver currentUserIdArgumentResolver) {
    this.allowedOrigins = allowedOrigins;
    this.currentUserIdArgumentResolver = currentUserIdArgumentResolver;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowedOrigins(allowedOrigins)
        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true);
  }

  @Override
  public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(currentUserIdArgumentResolver);
  }

  /**
   * 비밀번호 인코더 빈을 생성합니다.
   *
   * <p>SecurityConfig에 선언하지 않고 별도의 설정 클래스로 분리한 이유는, 순환 참조 문제를 방지하기 위함입니다.
   *
   * @return PasswordEncoder 인스턴스
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
