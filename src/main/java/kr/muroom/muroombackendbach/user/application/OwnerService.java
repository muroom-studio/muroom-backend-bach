package kr.muroom.muroombackendbach.user.application;

import static kr.muroom.muroombackendbach.user.presentation.dto.OwnerDto.*;

import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.terms.domain.entity.OwnerAgreement;
import kr.muroom.muroombackendbach.terms.domain.entity.Term;
import kr.muroom.muroombackendbach.terms.domain.repository.OwnerAgreementRepository;
import kr.muroom.muroombackendbach.terms.domain.repository.TermRepository;
import kr.muroom.muroombackendbach.terms.exception.TermErrorCode;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import kr.muroom.muroombackendbach.user.domain.repository.OwnerRepository;
import kr.muroom.muroombackendbach.user.exception.UserErrorCode;
import kr.muroom.muroombackendbach.user.presentation.dto.OwnerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

  private final OwnerRepository ownerRepository;
  private final UserService userService;
  private final OwnerMapper ownerMapper;
  private final OwnerAgreementRepository ownerAgreementRepository;
  private final TermRepository termRepository;

  @Transactional
  public Long registerOwner(OwnerSignUpDto request) {
    if (!userService.isNicknameAvailable(request.nickname())) {
      throw new BusinessException(UserErrorCode.ALREADY_EXIST_NICKNAME);
    }

    if (ownerRepository.existsByEmail(request.email())) {
      throw new BusinessException(UserErrorCode.ALREADY_EXIST_EMAIL);
    }

    Owner owner = ownerMapper.toEntity(request);
    Owner saved = ownerRepository.save(owner);

    // 2. 약관 조회
    List<Long> termIds = request.termIds();
    List<Term> terms = termRepository.findAllById(termIds);

    if (terms.size() != termIds.size()) {
      throw new BusinessException(TermErrorCode.NOT_EXIST_TERM);
    }

    // 3. OwnerAgreement 엔티티 생성
    List<OwnerAgreement> agreements = terms.stream()
        .map(t -> OwnerAgreement.of(owner, t))
        .toList();

    // 4. 일괄 저장
    ownerAgreementRepository.saveAll(agreements);

    return saved.getId();
  }

  /**
   * 이메일 중복 확인 API
   */
  public EmailCheckResponse checkEmail(String email) {
    boolean isUnavailable = ownerRepository.existsByEmail(email);
    String message = isUnavailable
        ? "이미 사용 중인 이메일입니다."
        : "사용 가능한 이메일입니다.";

    return new EmailCheckResponse(!isUnavailable, message);
  }
}
