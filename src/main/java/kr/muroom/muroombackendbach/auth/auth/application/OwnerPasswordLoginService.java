package kr.muroom.muroombackendbach.auth.auth.application;

import static kr.muroom.muroombackendbach.auth.auth.exception.AuthErrorCode.*;

import kr.muroom.muroombackendbach.auth.auth.exception.AuthErrorCode;
import kr.muroom.muroombackendbach.auth.auth.presentation.dto.request.OwnerLoginRequest;
import kr.muroom.muroombackendbach.auth.auth.presentation.dto.response.OwnerLoginResponse;
import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider;
import kr.muroom.muroombackendbach.auth.jwt.RefreshTokenService;
import kr.muroom.muroombackendbach.auth.login.OwnerPrincipal;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerPasswordLoginService {

  private final AuthenticationManager ownerAuthenticationManager;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public OwnerLoginResponse login(OwnerLoginRequest request) {
    Authentication authentication;
    try {
      authentication = ownerAuthenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.email(), request.password())
      );
    } catch (BadCredentialsException e) {
      throw new BusinessException(LOGIN_FAIL);
    }

    if (!(authentication.getPrincipal() instanceof OwnerPrincipal ownerPrincipal)) {
      throw new BusinessException(LOGIN_FAIL);
    }

    Long ownerId = ownerPrincipal.getOwnerId();

    // access/refresh 발급 + redis 저장 (OAuth와 동일 패턴)
    String accessToken = jwtTokenProvider.createAccessToken(ownerId);
    JwtTokenProvider.RefreshIssue refreshIssue = jwtTokenProvider.createRefreshToken(ownerId);

    refreshTokenService.save(ownerId, refreshIssue.jti(), refreshIssue.expiresAt());

    return OwnerLoginResponse.of(accessToken, refreshIssue.token(), ownerId);
  }
}
