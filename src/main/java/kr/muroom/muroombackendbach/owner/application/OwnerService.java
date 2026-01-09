package kr.muroom.muroombackendbach.owner.application;

import static kr.muroom.muroombackendbach.owner.exception.OwnerErrorCode.EMAIL_ALREADY_EXISTS;
import static kr.muroom.muroombackendbach.owner.exception.OwnerErrorCode.NICKNAME_ALREADY_EXISTS;
import static kr.muroom.muroombackendbach.owner.exception.OwnerErrorCode.PHONENUMBER_ALREADY_EXISTS;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider;
import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider.PhoneVerifyPayload;
import kr.muroom.muroombackendbach.auth.jwt.RefreshTokenService;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.musician.domain.entity.UserStatus;
import kr.muroom.muroombackendbach.owner.domain.entity.Owner;
import kr.muroom.muroombackendbach.owner.domain.repository.OwnerRepository;
import kr.muroom.muroombackendbach.owner.presentation.dto.request.OwnerSignupRequest;
import kr.muroom.muroombackendbach.owner.presentation.dto.response.OwnerSignupResponse;
import kr.muroom.muroombackendbach.terms.domain.entity.OwnerAgreement;
import kr.muroom.muroombackendbach.terms.domain.entity.TargetRole;
import kr.muroom.muroombackendbach.terms.domain.entity.Term;
import kr.muroom.muroombackendbach.terms.domain.repository.OwnerAgreementRepository;
import kr.muroom.muroombackendbach.terms.domain.repository.TermRepository;
import kr.muroom.muroombackendbach.terms.exception.TermErrorCode;
import kr.muroom.muroombackendbach.terms.presentation.dto.response.TermDetailResponse;
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

  /**
   * Owner 회원가입
   * <p>
   * 1) 휴대폰 인증 토큰 파싱 → 인증된 phoneNumber 확보 2) 중복(전화/이메일/닉네임) 검증 3) 약관 존재 + 필수 약관 미동의 검증 4) Owner 저장
   * 5) 약관 동의 저장 6) JWT 발급
   */
  @Transactional
  public OwnerSignupResponse registerOwner(OwnerSignupRequest request) {
    // 1. 휴대폰 인증 토큰 검증 (여기서 인증 보장)
    PhoneVerifyPayload phoneVerifyPayload = jwtTokenProvider.parsePhoneVerifyToken(
        request.smsVerifyToken());
    String verifiedPhone = phoneVerifyPayload.phoneNumber();

    // 2. 중복 검증 (검증된 phone 사용)
    validateUniqueOwner(request, verifiedPhone);

    // 3. 약관 검증
    List<Long> agreedTermIds = distinctTermIds(request.termIds());
    List<Term> agreedTerms = loadAndValidateTerms(agreedTermIds);
    validateRequiredTermsAgreed(agreedTermIds);

    // 4. Owner 저장 (검증된 phone 사용)
    Owner savedOwner = saveOwner(request, verifiedPhone);

    // 5. 약관 동의 저장
    saveOwnerAgreements(savedOwner, agreedTerms);

    // 6. 토큰 발급
    return issueTokens(savedOwner.getId());
  }

  /**
   * 전화번호/이메일/닉네임 중복 검증 - phoneNumber는 반드시 "인증된 번호"를 사용
   */
  private void validateUniqueOwner(OwnerSignupRequest request, String verifiedPhone) {
    if (ownerRepository.existsByPhoneNumber(verifiedPhone)) {
      throw new BusinessException(PHONENUMBER_ALREADY_EXISTS);
    }
    if (ownerRepository.existsByEmail(request.email())) {
      throw new BusinessException(EMAIL_ALREADY_EXISTS);
    }
    if (ownerRepository.existsByNickname(request.nickname())) {
      throw new BusinessException(NICKNAME_ALREADY_EXISTS);
    }
  }

  /**
   * 요청 termIds 중복 제거
   */
  private List<Long> distinctTermIds(List<Long> termIds) {
    return termIds.stream().distinct().toList();
  }

  /**
   * termIds로 약관 조회 및 존재 검증
   */
  private List<Term> loadAndValidateTerms(List<Long> termIds) {
    List<Term> terms = termRepository.findAllById(termIds);
    if (terms.size() != termIds.size()) {
      throw new BusinessException(TermErrorCode.NOT_EXIST_TERM);
    }
    return terms;
  }

  /**
   * 최신 약관 기준 필수 약관 동의 여부 검증
   */
  private void validateRequiredTermsAgreed(List<Long> agreedTermIds) {
    Set<Long> requiredTermIds =
        termRepository.findLatestTermsByRoleAndTypes(TargetRole.OWNER).stream()
            .filter(TermDetailResponse::isMandatory)
            .map(TermDetailResponse::termId)
            .map(Long::valueOf)
            .collect(Collectors.toSet());

    if (!Set.copyOf(agreedTermIds).containsAll(requiredTermIds)) {
      throw new BusinessException(TermErrorCode.REQUIRED_TERM_NOT_AGREED);
    }
  }

  /**
   * 비밀번호 암호화 후 Owner 저장
   */
  private Owner saveOwner(OwnerSignupRequest request, String verifiedPhone) {
    String encodedPassword = passwordEncoder.encode(request.password());

    Owner owner = Owner.builder()
        .nickname(request.nickname())
        .status(UserStatus.ACTIVE)
        .phoneNumber(verifiedPhone)
        .email(request.email())
        .name(request.name())
        .experienceYears(1)
        .password(encodedPassword)
        .build();

    return ownerRepository.save(owner);
  }

  /**
   * Owner ↔ 약관 동의 관계 저장
   */
  private void saveOwnerAgreements(Owner owner, List<Term> terms) {
    List<OwnerAgreement> agreements = terms.stream()
        .map(term -> OwnerAgreement.of(owner, term))
        .toList();

    ownerAgreementRepository.saveAll(agreements);
  }

  /**
   * Access / Refresh 토큰 발급
   */
  private OwnerSignupResponse issueTokens(Long ownerId) {
    String accessToken = jwtTokenProvider.createAccessToken(ownerId);
    JwtTokenProvider.RefreshIssue refreshIssue = jwtTokenProvider.createRefreshToken(ownerId);

    refreshTokenService.save(ownerId, refreshIssue.jti(), refreshIssue.expiresAt());

    return new OwnerSignupResponse(accessToken, refreshIssue.token(), String.valueOf(ownerId));
  }
}
