package kr.muroom.muroombackendbach.user.application;

import static kr.muroom.muroombackendbach.instrument.exception.InstrumentErrorCode.NOT_EXIST_INSTRUMENT;
import static kr.muroom.muroombackendbach.user.exception.MusicianErrorCode.DUPLICATE_PHONE_NUMBER;
import static kr.muroom.muroombackendbach.user.exception.MusicianErrorCode.MUSICIAN_NOT_FOUND;
import static kr.muroom.muroombackendbach.user.exception.MyStudioErrorCode.MY_STUDIO_NOT_FOUND;
import static kr.muroom.muroombackendbach.user.exception.SocialAccountErrorCode.SOCIAL_ACCOUNT_NOT_FOUND;
import static kr.muroom.muroombackendbach.user.exception.UserErrorCode.ALREADY_EXIST_NICKNAME;

import java.util.List;
import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider;
import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider.RefreshIssue;
import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider.SignupPayload;
import kr.muroom.muroombackendbach.auth.jwt.RefreshTokenService;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.instrument.domain.entity.Instrument;
import kr.muroom.muroombackendbach.instrument.domain.repository.InstrumentRepository;
import kr.muroom.muroombackendbach.terms.domain.entity.MusicianAgreement;
import kr.muroom.muroombackendbach.terms.domain.entity.Term;
import kr.muroom.muroombackendbach.terms.domain.repository.MusicianAgreementRepository;
import kr.muroom.muroombackendbach.terms.domain.repository.TermRepository;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.user.domain.entity.MyStudio;
import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
import kr.muroom.muroombackendbach.user.domain.entity.SocialAccount;
import kr.muroom.muroombackendbach.user.domain.entity.UserStatus;
import kr.muroom.muroombackendbach.user.domain.repository.MusicianRepository;
import kr.muroom.muroombackendbach.user.domain.repository.MyStudioRepository;
import kr.muroom.muroombackendbach.user.domain.repository.SocialAccountRepository;
import kr.muroom.muroombackendbach.user.presentation.dto.request.MusicianSignupRequest;
import kr.muroom.muroombackendbach.user.presentation.dto.request.UpdateMusicianProfileRequest;
import kr.muroom.muroombackendbach.user.presentation.dto.response.MusicianProfileResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.response.MusicianProfileResponse.MyStudioInfo;
import kr.muroom.muroombackendbach.user.presentation.dto.response.MusicianSignupResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.response.MusicianSimpleProfileResponse;
import kr.muroom.muroombackendbach.user.presentation.dto.response.MusicianSimpleProfileResponse.InstrumentSimpleInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MusicianService {

  private final MusicianRepository musicianRepository;
  private final UserService userService;
  private final MusicianAgreementRepository musicianAgreementRepository;
  private final TermRepository termRepository;
  private final SocialAccountRepository socialAccountRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final InstrumentRepository instrumentRepository;
  private final MyStudioRepository myStudioRepository;
  private final RefreshTokenService refreshTokenService;

  @Transactional
  public MusicianSignupResponse registerMusician(MusicianSignupRequest request) {
    // 0. token 검증
    SignupPayload signupPayload = jwtTokenProvider.parseSignupToken(request.signupToken());

    OAuthProvider provider = OAuthProvider.fromRegistrationId(signupPayload.provider());
    String providerUserId = signupPayload.providerId();

    // 1. 이름/전화번호로 기존 뮤지션 조회, 없으면 신규 생성
    Musician musician = findOrRegisterMusician(request);

    // 2. 토큰 발급
    Long musicianId = musician.getId();
    String accessToken = jwtTokenProvider.createAccessToken(musicianId);
    RefreshIssue refreshToken = jwtTokenProvider.createRefreshToken(musicianId);

    // 2.1 Redis 토큰 저장
    refreshTokenService.save(musicianId, refreshToken.jti(), refreshToken.expiresAt());

    // 3. 소셜 계정 연결 (이미 연결되어 있으면 아무 작업 안 함)
    linkSocialAccountIfNecessary(musician, provider, providerUserId);

    // 4. 나의 작업실 생성
    createMyStudio(request, musician);

    return new MusicianSignupResponse(accessToken, refreshToken.token(), musicianId);
  }

  /**
   * 이름 + 전화번호로 기존 뮤지션 조회, 없으면 신규 가입
   */
  private Musician findOrRegisterMusician(MusicianSignupRequest request) {
    return musicianRepository.findByNameAndPhoneNumber(request.name(), request.phoneNumber())
        .orElseGet(() -> registerNewMusician(request));
  }

  /**
   * 나의 작업실 생성
   */
  private void createMyStudio(MusicianSignupRequest request, Musician musician) {
    MyStudio myStudio = MyStudio.builder()
        .musician(musician)
        .name(request.studioName())
        .roadAddress(request.juso())
        .detailAddress(request.detailJuso())
        .build();

    myStudioRepository.save(myStudio);
  }

  /**
   * 기존 뮤지션에게 소셜 계정을 연결 (이미 연결된 경우 스킵)
   */
  private void linkSocialAccountIfNecessary(
      Musician musician,
      OAuthProvider provider,
      String providerUserId) {
    boolean alreadyLinked = socialAccountRepository
        .existsByMusicianAndProviderAndProviderUserId(
            musician,
            provider,
            providerUserId
        );

    if (alreadyLinked) {
      return;
    }

    SocialAccount socialAccount = SocialAccount.builder()
        .musician(musician)
        .provider(provider)
        .providerUserId(providerUserId)
        .build();

    socialAccountRepository.save(socialAccount);
  }

  /**
   * 신규 뮤지션 가입 + 약관 동의 처리 (소셜 계정 연결은 바깥에서 처리)
   */
  private Musician registerNewMusician(MusicianSignupRequest request) {
    validateNickname(request.nickname());
    List<Term> terms = loadAndValidateTerms(request.termIds());

    Instrument instrument = instrumentRepository.findById(request.instrumentId())
        .orElseThrow(() -> new BusinessException(NOT_EXIST_INSTRUMENT));

    Musician musician = Musician.builder()
        .name(request.name())
        .phoneNumber(request.phoneNumber())
        .nickname(request.nickname())
        .status(UserStatus.ACTIVE)
        .instrument(instrument)
        .build();

    musicianRepository.save(musician);
    saveAgreements(musician, terms);

    return musician;
  }

  /**
   * 닉네임 중복 검증
   */
  private void validateNickname(String nickname) {
    if (!userService.isNicknameAvailable(nickname)) {
      throw new IllegalArgumentException("이미 존재하는 닉네임 입니다.");
    }
  }

  /**
   * 약관 ID로 Term 조회 + 검증
   */
  private List<Term> loadAndValidateTerms(List<Long> termIds) {
    List<Term> terms = termRepository.findAllById(termIds);

    if (terms.size() != termIds.size()) {
      throw new IllegalArgumentException("존재하지 않는 약관 ID가 포함되어 있습니다.");
    }

    return terms;
  }

  /**
   * MusicianAgreement 생성 & 저장
   */
  private void saveAgreements(Musician musician, List<Term> terms) {
    List<MusicianAgreement> agreements = terms.stream()
        .map(term -> MusicianAgreement.of(musician, term))
        .toList();

    musicianAgreementRepository.saveAll(agreements);
  }

  @Transactional(readOnly = true)
  public MusicianSimpleProfileResponse getMusicianSimpleProfile(Long musicianId) {
    Musician musician = musicianRepository.findById(musicianId)
        .orElseThrow(() -> new BusinessException(MUSICIAN_NOT_FOUND));

    return MusicianSimpleProfileResponse.builder()
        .musicianId(musician.getId())
        .nickname(musician.getNickname())
        .musicianInstrument(InstrumentSimpleInfo.from(musician.getInstrument()))
        .build();
  }

  @Transactional(readOnly = true)
  public MusicianProfileResponse getMusicianProfile(Long musicianId) {
    Musician musician = musicianRepository.findById(musicianId)
        .orElseThrow(() -> new BusinessException(MUSICIAN_NOT_FOUND));

    SocialAccount socialAccount = socialAccountRepository.findByMusicianId(musicianId)
        .orElseThrow(() -> new BusinessException(SOCIAL_ACCOUNT_NOT_FOUND));

    MyStudio myStudio = myStudioRepository.findFirstByMusicianId(musicianId)
        .orElseThrow(() -> new BusinessException(MY_STUDIO_NOT_FOUND));

    return MusicianProfileResponse.builder()
        .musicianId(musician.getId())
        .nickname(musician.getNickname())
        .musicianInstrument(InstrumentSimpleInfo.from(musician.getInstrument()))
        .snsAccount(socialAccount.getProvider())
        .myStudio(MyStudioInfo.from(myStudio))
        .build();
  }

  @Transactional
  public void updateMyProfile(Long musicianId, UpdateMusicianProfileRequest request) {
    Musician musician = musicianRepository.findById(musicianId)
        .orElseThrow(() -> new BusinessException(MUSICIAN_NOT_FOUND));

    updateNickname(musician, request);
    updateInstrument(musician, request);
    updatePhone(musician, request);
    updateStudioIfNeeded(musician, request);
  }

  private void updateNickname(Musician musician, UpdateMusicianProfileRequest request) {
    if (request.nickname() == null) {
      return;
    }

    if (musicianRepository.existsByNickname(request.nickname())) {
      throw new BusinessException(ALREADY_EXIST_NICKNAME);
    }

    musician.changeNickname(request.nickname());
  }

  private void updateInstrument(Musician musician, UpdateMusicianProfileRequest request) {
    if (request.instrumentId() == null) {
      return;
    }

    Instrument instrument = instrumentRepository.findById(request.instrumentId())
        .orElseThrow(() -> new BusinessException(NOT_EXIST_INSTRUMENT));

    musician.changeInstrument(instrument);
  }

  private void updatePhone(Musician musician, UpdateMusicianProfileRequest request) {
    if (request.phone() == null) {
      return;
    }

    // 동일 번호면 스킵 (불필요 검증/변경 방지)
    if (request.phone().equals(musician.getPhoneNumber())) {
      return;
    }

    // 중복 체크
    if (musicianRepository.existsByPhoneNumber(request.phone())) {
      throw new BusinessException(DUPLICATE_PHONE_NUMBER);
    }

    musician.changePhone(request.phone());
  }

  private void updateStudioIfNeeded(Musician musician, UpdateMusicianProfileRequest request) {
    boolean hasStudioUpdate =
        request.studioName() != null ||
            request.roadAddress() != null ||
            request.detailAddress() != null;

    if (!hasStudioUpdate) {
      return;
    }

    MyStudio myStudio = myStudioRepository.findFirstByMusicianId(musician.getId())
        .orElseThrow(() -> new BusinessException(MY_STUDIO_NOT_FOUND));

    myStudio.changeMyStudio(
        request.studioName(),
        request.roadAddress(),
        request.detailAddress()
    );
  }

  public void dev() {
    
  }
}
