package kr.muroom.muroombackendbach.user.application;

import kr.muroom.muroombackendbach.terms.domain.entity.OwnerAgreement;
import kr.muroom.muroombackendbach.terms.domain.entity.Term;
import kr.muroom.muroombackendbach.terms.domain.repository.OwnerAgreementRepository;
import kr.muroom.muroombackendbach.terms.domain.repository.TermRepository;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import kr.muroom.muroombackendbach.user.domain.repository.OwnerRepository;
import kr.muroom.muroombackendbach.user.presentation.dto.OwnerDto;
import kr.muroom.muroombackendbach.user.presentation.dto.OwnerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerService {
    private final OwnerRepository ownerRepository;
    private final UserService userService;
    private final OwnerMapper ownerMapper;
    private final OwnerAgreementRepository ownerAgreementRepository;
    private final TermRepository termRepository;

    @Transactional
    public void registerOwner(OwnerDto.OwnerSignUpDto request) {
        if(!userService.isNicknameAvailable(request.nickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임 입니다.");
        }

        Owner owner = ownerMapper.toEntity(request);
        ownerRepository.save(owner);

        // 2. 약관 조회
        List<Long> termIds = request.termIds();
        List<Term> terms = termRepository.findAllById(termIds);

        if (terms.size() != termIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 약관 ID가 포함되어 있습니다.");
        }

        // 3. MusicianAgreement 엔티티 생성
        List<OwnerAgreement> agreements = terms.stream()
                .map(t -> OwnerAgreement.of(owner, t))
                .toList();

        // 4. 일괄 저장
        ownerAgreementRepository.saveAll(agreements);
    }
}
