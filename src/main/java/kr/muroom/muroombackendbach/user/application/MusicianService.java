package kr.muroom.muroombackendbach.user.application;

import jakarta.transaction.Transactional;
import kr.muroom.muroombackendbach.terms.domain.entity.MusicianAgreement;
import kr.muroom.muroombackendbach.terms.domain.entity.Term;
import kr.muroom.muroombackendbach.terms.domain.repository.MusicianAgreementRepository;
import kr.muroom.muroombackendbach.terms.domain.repository.TermRepository;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.user.domain.repository.MusicianRepository;
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

    @Transactional
    public void registerMusician(MusicianDto.MusicianSignUpDto request) {
        // 0. 약관 ID 검증 (정책에 따라 필수/선택 처리 가능)
        List<Long> termIds = request.termIds();

        if(!userService.isNicknameAvailable(request.nickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임 입니다.");
        }

        // 1. 뮤지션 생성 & 저장
        Musician musician = musicianMapper.toEntity(request);
        musicianRepository.save(musician);

        // 2. 약관 조회
        List<Term> terms = termRepository.findAllById(termIds);

        if (terms.size() != termIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 약관 ID가 포함되어 있습니다.");
        }

        // 3. MusicianAgreement 엔티티 생성
        List<MusicianAgreement> agreements = terms.stream()
                .map(t -> MusicianAgreement.of(musician, t))
                .toList();

        // 4. 일괄 저장
        musicianAgreementRepository.saveAll(agreements);
    }
}
