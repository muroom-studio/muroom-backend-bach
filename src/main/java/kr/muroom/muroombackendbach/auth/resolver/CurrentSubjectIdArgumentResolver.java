package kr.muroom.muroombackendbach.auth.resolver;

import kr.muroom.muroombackendbach.auth.annotation.CurrentSubjectId;
import kr.muroom.muroombackendbach.auth.auth.exception.AuthErrorCode;
import kr.muroom.muroombackendbach.common.context.AnonymousUserContext;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import org.springframework.core.MethodParameter;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentSubjectIdArgumentResolver implements HandlerMethodArgumentResolver {

  private final ExpressionParser parser = new SpelExpressionParser();

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(CurrentSubjectId.class)
        && String.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(
      MethodParameter parameter,
      ModelAndViewContainer mavContainer,
      NativeWebRequest webRequest,
      WebDataBinderFactory binderFactory
  ) {
    CurrentSubjectId annotation = parameter.getParameterAnnotation(CurrentSubjectId.class);
    boolean required = annotation != null && annotation.required();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // 1) 로그인 사용자면 "U:{userId}"
    if (authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken)) {

      Object principal = authentication.getPrincipal();
      try {
        StandardEvaluationContext context = new StandardEvaluationContext(principal);
        Expression expression = parser.parseExpression("userId");
        Object userId = expression.getValue(context);

        if (userId == null) {
          if (required) {
            throw new BusinessException(AuthErrorCode.UNAUTHORIZED);
          }
          return null;
        }

        return "U:" + userId;
      } catch (Exception e) {
        if (required) {
          throw new BusinessException(AuthErrorCode.UNAUTHORIZED);
        }
        return null;
      }
    }

    // 2) 비회원이면 "G:{anonymousUserId}" (AnonymousUserFilter가 세팅)
    String anonymousUserId = AnonymousUserContext.getAnonymousUserId();

    if (anonymousUserId == null || anonymousUserId.isBlank()) {
      if (required) {
        throw new BusinessException(AuthErrorCode.UNAUTHORIZED);
      }
      return null;
    }

    return "G:" + anonymousUserId;
  }
}
