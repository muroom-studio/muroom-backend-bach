package kr.muroom.muroombackendbach.map.config;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JTS(Geometry) 관련 설정을 제공하는 구성 클래스입니다.
 */
@Configuration
public class JtsConfig {

  /**
   * GeometryFactory 빈을 생성합니다.
   *
   * <p>SRID 4326(WGS84 좌표계)을 사용하도록 설정됩니다.
   *
   * @return GeometryFactory 인스턴스
   */
  @Bean
  public GeometryFactory geometryFactory() {
    return new GeometryFactory(new PrecisionModel(), 4326);
  }
}
