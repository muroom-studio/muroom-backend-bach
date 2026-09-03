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
import kr.muroom.muroombackendbach.common.util.AnonymousIdSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class AnonymousUserFilter implements Filter {

  public static final String ANONYMOUS_USER_ID_HEADER_NAME = "X-Anonymous-ID";
  public static final String ANONYMOUS_USER_ID_COOKIE_NAME = "anonymous_user_id";
  public static final String SET_COOKIE_HEADER_NAME = "Set-Cookie";
  public static final String ANONYMOUS_USER_SIG_COOKIE_NAME = "anonymous_user_sig";

  private final AnonymousIdSigner signer;

  public AnonymousUserFilter(
          @Value("${jwt.secret-key}") String secret
  ) {
    this.signer = new AnonymousIdSigner(secret);
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain) throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) servletRequest;
    HttpServletResponse res = (HttpServletResponse) servletResponse;

    try {
      Optional<String> anonIdOpt = getCookie(req, ANONYMOUS_USER_ID_COOKIE_NAME);
      Optional<String> sigOpt = getCookie(req, ANONYMOUS_USER_SIG_COOKIE_NAME);

      String anonId = null;

      // 1) 쿠키가 둘 다 있고, 서명 검증이 통과하면 신뢰
      if (anonIdOpt.isPresent() && sigOpt.isPresent()
              && signer.verify(anonIdOpt.get(), sigOpt.get())) {
        anonId = anonIdOpt.get();
      }

      // 2) 없거나/검증 실패면 새로 발급
      if (anonId == null) {
        anonId = UUID.randomUUID().toString();
        String sig = signer.sign(anonId);

        ResponseCookie idCookie = ResponseCookie.from(ANONYMOUS_USER_ID_COOKIE_NAME, anonId)
                .maxAge(60 * 60 * 24 * 365)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .build();

        ResponseCookie sigCookie = ResponseCookie.from(ANONYMOUS_USER_SIG_COOKIE_NAME, sig)
                .maxAge(60 * 60 * 24 * 365)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .build();

        res.addHeader(SET_COOKIE_HEADER_NAME, idCookie.toString());
        res.addHeader(SET_COOKIE_HEADER_NAME, sigCookie.toString());
      }

      AnonymousUserContext.setAnonymousUserId(anonId);

      filterChain.doFilter(servletRequest, servletResponse);
    } finally {
      AnonymousUserContext.clear();
    }
  }

  private Optional<String> getCookie(HttpServletRequest request, String name) {
    if (request.getCookies() == null) return Optional.empty();
    return Arrays.stream(request.getCookies())
            .filter(c -> name.equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst();
  }
}
