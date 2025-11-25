package kr.muroom.muroombackendbach.map.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KakaoFeignClientConfig {

  private final String restApiKey;

  public KakaoFeignClientConfig(@Value("${kakao.api.rest-api-key}") String restApiKey) {
    this.restApiKey = restApiKey;
  }

  @Bean
  public RequestInterceptor requestInterceptor() {
    return requestTemplate -> {
      requestTemplate.header("Authorization", "KakaoAK " + restApiKey);
    };
  }
}
