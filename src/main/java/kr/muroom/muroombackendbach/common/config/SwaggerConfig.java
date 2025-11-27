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

  private final String serverUrl;
  private final String springdocVersion;
  private final String springdocTitle;
  private final String springdocDescription;

  public SwaggerConfig(
      @Value("${server.url}") String serverUrl,
      @Value("${springdoc.version}") String springdocVersion,
      @Value("${springdoc.title}") String springdocTitle,
      @Value("${springdoc.description}") String springdocDescription) {
    this.serverUrl = serverUrl;
    this.springdocVersion = springdocVersion;
    this.springdocTitle = springdocTitle;
    this.springdocDescription = springdocDescription;
  }

  @Bean
  public OpenAPI openApi() {
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
            .title(springdocTitle)
            .description(springdocDescription)
            .version(springdocVersion))
        .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme));
  }
}