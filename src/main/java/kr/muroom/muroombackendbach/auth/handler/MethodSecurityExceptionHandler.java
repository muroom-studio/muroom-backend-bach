package kr.muroom.muroombackendbach.auth.handler;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class MethodSecurityExceptionHandler {

  @ExceptionHandler(AuthorizationDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public void handle(AuthorizationDeniedException ex) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    boolean notLoggedIn =
        auth == null ||
            auth instanceof AnonymousAuthenticationToken ||
            !auth.isAuthenticated();

    if (notLoggedIn) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    // 로그인은 되어 있는데 권한이 없음 -> 403 유지
    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
  }
}
