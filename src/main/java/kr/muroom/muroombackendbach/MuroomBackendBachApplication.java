package kr.muroom.muroombackendbach;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MuroomBackendBachApplication {

  public static void main(String[] args) {

    SpringApplication.run(MuroomBackendBachApplication.class, args);
  }

}
