package kr.muroom.muroombackendbach.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 관련 설정 클래스입니다.
 */
@Configuration
public class S3Config {

  private final String region;

  /**
   * 생성자 주입을 통해 AWS 리전을 설정합니다.
   *
   * @param region AWS 리전
   */
  public S3Config(
      @Value("${cloud.aws.region.static}") String region) {
    this.region = region;
  }

  /**
   * S3 Client 빈을 생성합니다.
   *
   * @return S3Client 인스턴스
   */
  @Bean
  public S3Client s3Client() {
    return S3Client.builder()
        .region(Region.of(region))
        .build();
  }

  /**
   * S3 Presigner 빈을 생성합니다.
   *
   * @return S3Presigner 인스턴스
   */
  @Bean
  public S3Presigner s3Presigner() {
    return S3Presigner.builder()
        .region(Region.of(region))
        .build();
  }
}
