package kr.muroom.muroombackendbach.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!prod")
@Configuration
public class SwaggerConfig {

  @Value("${server.url}")
  private String serverUrl;

  @Bean
  public OpenAPI openAPI() {
    final String securitySchemeName = "Authorization";
    SecurityScheme securityScheme = new SecurityScheme()
        .name(securitySchemeName)
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")
        .in(SecurityScheme.In.HEADER)
        .name("Authorization");

    return new OpenAPI()
        .servers(List.of(new Server().url(serverUrl).description("Current Environment Server")))
        .info(new Info()
            .title("Muroom Project API")
            .description("Muroom 프로젝트 API 명세서입니다. 'Authorize' 버튼을 눌러 JWT 토큰을 입력해주세요.")
            .version("1.0.0"))
        .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme));
  }
}