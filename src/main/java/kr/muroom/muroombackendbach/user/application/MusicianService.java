package kr.muroom.muroombackendbach.user.application;

import jakarta.transaction.Transactional;
import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider;
import kr.muroom.muroombackendbach.terms.domain.entity.MusicianAgreement;
import kr.muroom.muroombackendbach.terms.domain.entity.Term;
import kr.muroom.muroombackendbach.terms.domain.repository.MusicianAgreementRepository;
import kr.muroom.muroombackendbach.terms.domain.repository.TermRepository;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.user.domain.entity.OAuthProvider;
import kr.muroom.muroombackendbach.user.domain.entity.SocialAccount;
import kr.muroom.muroombackendbach.user.domain.repository.MusicianRepository;
import kr.muroom.muroombackendbach.user.domain.repository.SocialAccountRepository;
import kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto;
import kr.muroom.muroombackendbach.user.presentation.dto.MusicianMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicianService {

    private final MusicianRepository musicianRepository;
    private final MusicianMapper musicianMapper;
    private final UserService userService;
    private final MusicianAgreementRepository musicianAgreementRepository;
    private final TermRepository termRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public MusicianDto.MusicianSignUpResponse registerMusician(MusicianDto.MusicianSignUpDto request) {
        JwtTokenProvider.SignupPayload signupPayload =
                jwtTokenProvider.parseSignupToken(request.signupToken());

        OAuthProvider provider = OAuthProvider.fromRegistrationId(signupPayload.provider());
        String providerUserId = signupPayload.providerId();

        Long musicianId = musicianRepository.findByNameAndPhoneNumber(request.name(), request.phoneNumber())
                .map(existing -> {
                    linkSocialAccountIfNecessary(existing, provider, providerUserId);
                    return existing.getId();
                })
                .orElseGet(() -> {
                    Musician musician = registerNewMusician(request, provider, providerUserId);
                    return musician.getId();
                });

        String accessToken = jwtTokenProvider.createToken(musicianId);
        return new MusicianDto.MusicianSignUpResponse(accessToken, musicianId);
    }

    /**
     * 기존 뮤지션에게 소셜 계정을 연결하는 흐름
     */
    private void linkSocialAccountIfNecessary(Musician musician,
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
     * 신규 뮤지션 가입 + 소셜 계정 연결 + 약관 동의 처리
     */
    private Musician registerNewMusician(MusicianDto.MusicianSignUpDto request,
                                         OAuthProvider provider,
                                         String providerUserId) {

        validateNickname(request.nickname());
        List<Term> terms = loadAndValidateTerms(request.termIds());

        Musician musician = musicianMapper.toEntity(request);
        musicianRepository.save(musician);

        linkSocialAccount(musician, provider, providerUserId);
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
     * 소셜 계정 생성/연결
     */
    private void linkSocialAccount(Musician musician,
                                   OAuthProvider provider,
                                   String providerUserId) {

        SocialAccount socialAccount = SocialAccount.builder()
                .musician(musician)
                .provider(provider)
                .providerUserId(providerUserId)
                .build();

        socialAccountRepository.save(socialAccount);
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
}
