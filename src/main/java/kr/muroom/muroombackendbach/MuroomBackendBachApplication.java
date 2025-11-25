package kr.muroom.muroombackendbach;

import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * MuroomBackendBachApplication 클래스는 Spring Boot 애플리케이션의 진입점입니다.
 */
@EnableFeignClients
@EnableAsync
@SpringBootApplication
public class MuroomBackendBachApplication {

  /**
   * 애플리케이션 시작 후 호출되어 기본 시간대를 "Asia/Seoul"로 설정합니다.
   */
  @PostConstruct
  public void started() {

    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
  }

  /**
   * 애플리케이션의 메인 메서드로, SpringApplication을 사용하여 애플리케이션을 실행합니다.
   */
  public static void main(String[] args) {

    SpringApplication.run(MuroomBackendBachApplication.class, args);
  }

}
