package kr.muroom.muroombackendbach.auth.config;

import kr.muroom.muroombackendbach.auth.login.OwnerUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SecurityUserConfig {

  private final OwnerUserDetailsService ownerUserDetailsService;

  @Bean
  public PasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public DaoAuthenticationProvider ownerDaoAuthenticationProvider(PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(); // TODO: deprecated warning
    provider.setUserDetailsService(ownerUserDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public AuthenticationManager ownerAuthenticationManager(
      DaoAuthenticationProvider ownerDaoAuthenticationProvider) {
    return new ProviderManager(ownerDaoAuthenticationProvider);
  }
}
