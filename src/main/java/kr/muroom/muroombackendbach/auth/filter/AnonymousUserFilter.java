package kr.muroom.muroombackendbach.auth.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import kr.muroom.muroombackendbach.common.context.AnonymousUserContext;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AnonymousUserFilter implements Filter {

  public static final String ANONYMOUS_USER_ID_HEADER_NAME = "X-Anonymous-ID";
  public static final String ANONYMOUS_USER_ID_COOKIE_NAME = "anonymous_user_id";
  public static final String SET_COOKIE_HEADER_NAME = "Set-Cookie";

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {

    HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
    HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

    try {
      Optional<String> anonymousUserId = getAnonymousUserIdFromHeader(httpServletRequest);

      if (anonymousUserId.isEmpty()) {
        anonymousUserId = getAnonymousUserIdFromCookie(httpServletRequest);

        if (anonymousUserId.isEmpty()) {
          String newAnonymousUserId = UUID.randomUUID().toString();
          ResponseCookie anonymousUserCookieForWeb = ResponseCookie
              .from(ANONYMOUS_USER_ID_COOKIE_NAME, newAnonymousUserId)
              .maxAge(60 * 60 * 24 * 365) // 1년
              .path("/")
              .httpOnly(true)
              .secure(true)
              .sameSite("Lax") // 외부에서 muroom 도메인으로 접근하는 경우에도 쿠키가 전송되도록 설정
              .build();
          httpServletResponse.addHeader(SET_COOKIE_HEADER_NAME,
              anonymousUserCookieForWeb.toString());

          anonymousUserId = Optional.of(newAnonymousUserId);
        }
      }

      anonymousUserId.ifPresent(AnonymousUserContext::setAnonymousUserId);

      filterChain.doFilter(servletRequest, servletResponse);
    } finally {
      AnonymousUserContext.clear();
    }
  }

  private Optional<String> getAnonymousUserIdFromHeader(HttpServletRequest request) {
    String headerValue = request.getHeader(ANONYMOUS_USER_ID_HEADER_NAME);
    return Optional.ofNullable(headerValue);
  }

  private Optional<String> getAnonymousUserIdFromCookie(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    return Arrays.stream(request.getCookies())
        .filter(cookie -> ANONYMOUS_USER_ID_COOKIE_NAME.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }
}
