package kr.muroom.muroombackendbach.auth.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  @Bean
  public SecurityFilterChain apiSecurityfilterChain(HttpSecurity http) throws Exception {
    http
        .cors(Customizer.withDefaults())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement((session) -> session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        //.anonymous(AbstractHttpConfigurer::disable)
        .exceptionHandling(eh -> eh
            .authenticationEntryPoint(
                (req, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
            .accessDeniedHandler((req, res, ex) -> res.sendError(HttpServletResponse.SC_FORBIDDEN))
        )
        .authorizeHttpRequests((auth) -> auth
            .requestMatchers(
                "/api/ping",
                "/actuator/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/v3/api-docs/**",
                "/api/v1/auth/**",
                "/api/v1/musicians/register",
                "/api/v1/musicians/nickname/check",
                "/api/v1/musicians/phone/check",
                "/api/v1/studios/**",
                "/api/v1/faq/**",
                "/api/v1/inquiry-categories/**",
                "/api/v1/instruments/**",
                "/api/v1/search-histories/**",
                "/api/v1/subways/**",
                "/api/v1/terms/**",
                "/docs",
                "/api/v1/sms/**"
            ).permitAll()
            .anyRequest().authenticated()
        );
    return http.build();
  }
}
