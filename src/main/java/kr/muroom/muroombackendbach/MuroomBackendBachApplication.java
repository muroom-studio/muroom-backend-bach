package kr.muroom.muroombackendbach;

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
   * 애플리케이션의 메인 메서드로, SpringApplication을 사용하여 애플리케이션을 실행합니다.
   */
  public static void main(String[] args) {

    SpringApplication.run(MuroomBackendBachApplication.class, args);
  }

}
