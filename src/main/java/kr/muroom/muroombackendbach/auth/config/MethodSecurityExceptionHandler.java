package kr.muroom.muroombackendbach.auth.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MethodSecurityExceptionHandler {

  @ExceptionHandler(AuthorizationDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN) // 기본은 403으로 두고 아래에서 401로 분기
  public void handle(AuthorizationDeniedException ex) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    boolean notLoggedIn =
        auth == null ||
            auth instanceof AnonymousAuthenticationToken ||
            !auth.isAuthenticated();

    if (notLoggedIn) {
      // 로그인 필요 -> 401로 바꾸고 싶으면 ResponseStatus를 못 바꾸니 예외를 다시 던지는 방식 대신,
      // 아래처럼 ResponseStatusException을 던지거나, ResponseEntity로 처리하는 방식이 더 깔끔함.
      throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }

    // 로그인은 되어 있는데 권한이 없음 -> 403 유지
    throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN);
  }
}
