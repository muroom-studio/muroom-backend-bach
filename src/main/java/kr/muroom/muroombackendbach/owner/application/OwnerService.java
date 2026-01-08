package kr.muroom.muroombackendbach.owner.application;

import static kr.muroom.muroombackendbach.owner.exception.OwnerErrorCode.EMAIL_ALREADY_EXISTS;
import static kr.muroom.muroombackendbach.owner.exception.OwnerErrorCode.NICKNAME_ALREADY_EXISTS;
import static kr.muroom.muroombackendbach.owner.exception.OwnerErrorCode.PHONENUMBER_ALREADY_EXISTS;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider;
import kr.muroom.muroombackendbach.auth.jwt.RefreshTokenService;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.musician.domain.entity.UserStatus;
import kr.muroom.muroombackendbach.owner.domain.entity.Owner;
import kr.muroom.muroombackendbach.owner.domain.repository.OwnerRepository;
import kr.muroom.muroombackendbach.owner.presentation.dto.request.OwnerSignupRequest;
import kr.muroom.muroombackendbach.owner.presentation.dto.response.OwnerSignupResponse;
import kr.muroom.muroombackendbach.terms.domain.entity.OwnerAgreement;
import kr.muroom.muroombackendbach.terms.domain.entity.Term;
import kr.muroom.muroombackendbach.terms.domain.repository.OwnerAgreementRepository;
import kr.muroom.muroombackendbach.terms.domain.repository.TermRepository;
import kr.muroom.muroombackendbach.terms.exception.TermErrorCode;
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
  private final TermRepository termRepository;
  private final OwnerAgreementRepository ownerAgreementRepository;

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

    List<Long> termIds = request.termIds().stream().distinct().toList();
    List<Term> terms = termRepository.findAllById(termIds);

    // 2) 비밀번호 암호화
    String encodedPassword = passwordEncoder.encode(request.password());

    // 3) Owner 저장 (ownerId 필요!)
    Owner owner = Owner.builder()
        .nickname(request.nickname())
        .status(UserStatus.ACTIVE)
        .phoneNumber(request.phoneNumber())
        .email(request.email())
        .name(request.name())
        .experienceYears(1)
        .password(encodedPassword)
        .build();

    Owner savedOwner = ownerRepository.save(owner);

    // 4) OwnerAgreement 저장 (람다/stream)
    List<OwnerAgreement> agreements = terms.stream()
        .map(term -> OwnerAgreement.of(savedOwner, term))
        .toList();

    ownerAgreementRepository.saveAll(agreements);

    // 5) JWT 발급 + Refresh 저장
    Long ownerId = savedOwner.getId();

    String accessToken = jwtTokenProvider.createAccessToken(ownerId);
    JwtTokenProvider.RefreshIssue refreshIssue = jwtTokenProvider.createRefreshToken(ownerId);

    refreshTokenService.save(ownerId, refreshIssue.jti(), refreshIssue.expiresAt());

    return new OwnerSignupResponse(accessToken, refreshIssue.token(), String.valueOf(ownerId));
  }
}
