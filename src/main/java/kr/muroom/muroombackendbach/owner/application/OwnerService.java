package kr.muroom.muroombackendbach.owner.application;

import static kr.muroom.muroombackendbach.owner.exception.OwnerErrorCode.EMAIL_ALREADY_EXISTS;
import static kr.muroom.muroombackendbach.owner.exception.OwnerErrorCode.NICKNAME_ALREADY_EXISTS;
import static kr.muroom.muroombackendbach.owner.exception.OwnerErrorCode.PHONENUMBER_ALREADY_EXISTS;

import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider;
import kr.muroom.muroombackendbach.auth.jwt.RefreshTokenService;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.musician.domain.entity.UserStatus;
import kr.muroom.muroombackendbach.owner.domain.entity.Owner;
import kr.muroom.muroombackendbach.owner.domain.repository.OwnerRepository;
import kr.muroom.muroombackendbach.owner.presentation.dto.request.OwnerSignupRequest;
import kr.muroom.muroombackendbach.owner.presentation.dto.response.OwnerSignupResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OwnerService {

  private final OwnerRepository ownerRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public OwnerSignupResponse registerOwner(OwnerSignupRequest request) {
    if (ownerRepository.existsByPhoneNumber(request.phoneNumber())) {
      throw new BusinessException(PHONENUMBER_ALREADY_EXISTS);
    }

    if (ownerRepository.existsByEmail(request.email())) {
      throw new BusinessException(EMAIL_ALREADY_EXISTS);
    }

    if (ownerRepository.existsByNickname(request.nickname())) {
      throw new BusinessException(NICKNAME_ALREADY_EXISTS);
    }

    String encodedPassword = passwordEncoder.encode(request.password());

    // 3) Owner 저장
    Owner owner = Owner.builder()
        .nickname(request.nickname())
        .status(UserStatus.ACTIVE)
        .phoneNumber(request.phoneNumber())
        .email(request.email())
        .name(request.name())
        .experienceYears(1)
        .password(encodedPassword)
        .build();

    Owner saved = ownerRepository.save(owner);

    // 4) 회원가입 직후 JWT 발급 + refresh redis 저장 (OAuthLoginService 패턴과 동일)
    Long ownerId = saved.getId();

    String accessToken = jwtTokenProvider.createAccessToken(ownerId);
    JwtTokenProvider.RefreshIssue refreshIssue = jwtTokenProvider.createRefreshToken(ownerId);

    refreshTokenService.save(ownerId, refreshIssue.jti(), refreshIssue.expiresAt());

    // 5) 응답
    return new OwnerSignupResponse(accessToken, refreshIssue.token(), String.valueOf(ownerId));
  }
}
