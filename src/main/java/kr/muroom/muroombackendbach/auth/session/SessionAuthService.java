package kr.muroom.muroombackendbach.auth.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import kr.muroom.muroombackendbach.auth.auth.domain.entity.UserType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class SessionAuthService {

  private final SecurityContextRepository securityContextRepository =
      new HttpSessionSecurityContextRepository();

  public void login(HttpServletRequest request,
      HttpServletResponse response,
      UserType userType,
      Long userId) {

    SessionAuthPrincipal principal = new SessionAuthPrincipal(userType, userId);
    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + userType.name()));

    var authentication =
        new UsernamePasswordAuthenticationToken(principal, null, authorities);

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);

    SecurityContextHolder.setContext(context);
    securityContextRepository.saveContext(context, request, response);
  }
}
