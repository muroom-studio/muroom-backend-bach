package kr.muroom.muroombackendbach.auth.session;

import jakarta.servlet.http.Cookie;
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

  private static final String JSESSIONID = "JSESSIONID";
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

  /**
   * 세션 기반 로그아웃 - SecurityContextHolder 정리 - 세션에 저장된 SecurityContext 제거 - (옵션) 세션 invalidate +
   * JSESSIONID 쿠키 만료
   */
  public void logout(HttpServletRequest request, HttpServletResponse response) {
    // 1) 현재 스레드의 SecurityContext 비우기
    SecurityContextHolder.clearContext();

    // 2) 세션에 저장된 SecurityContext 제거 (spring security가 쓰는 키 제거)
    SecurityContext emptyContext = SecurityContextHolder.createEmptyContext();
    securityContextRepository.saveContext(emptyContext, request, response);

    // 3) 세션 자체 무효화 (권장)
    var session = request.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    // 4) JSESSIONID 쿠키 만료 (프론트/브라우저에 남은 쿠키 제거)
    expireCookie(response);
  }

  private void expireCookie(HttpServletResponse response) {
    Cookie cookie = new Cookie(JSESSIONID, "");
    cookie.setPath("/");
    cookie.setMaxAge(0);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    response.addCookie(cookie);
  }
}
