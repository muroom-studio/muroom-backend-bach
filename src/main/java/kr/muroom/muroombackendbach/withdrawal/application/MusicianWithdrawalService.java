package kr.muroom.muroombackendbach.withdrawal.application;

import static kr.muroom.muroombackendbach.musician.exception.MusicianErrorCode.MUSICIAN_NOT_FOUND;
import static kr.muroom.muroombackendbach.user.exception.SocialAccountErrorCode.SOCIAL_ACCOUNT_NOT_FOUND;
import static kr.muroom.muroombackendbach.withdrawal.exception.WithdrawalReasonErrorCode.NOT_EXIST_WITHDRAWAL_REASON;

import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import kr.muroom.muroombackendbach.musician.domain.repository.MusicianRepository;
import kr.muroom.muroombackendbach.user.domain.entity.SocialAccount;
import kr.muroom.muroombackendbach.user.domain.repository.SocialAccountRepository;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.MusicianWithdrawal;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.WithdrawalReason;
import kr.muroom.muroombackendbach.withdrawal.domain.repository.MusicianWithdrawalRepository;
import kr.muroom.muroombackendbach.withdrawal.domain.repository.WithdrawalReasonRepository;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.RegisterMusicianWithdrawalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MusicianWithdrawalService {

  private final MusicianWithdrawalRepository musicianWithdrawalRepository;
  private final WithdrawalReasonRepository withdrawalReasonRepository;
  private final SocialAccountRepository socialAccountRepository;
  private final MusicianRepository musicianRepository;

  @Transactional
  public void register(Long musicianId, RegisterMusicianWithdrawalRequest request) {
    Musician musician = musicianRepository.findById(musicianId)
        .orElseThrow(() -> new BusinessException(MUSICIAN_NOT_FOUND));

    WithdrawalReason withdrawalReason = withdrawalReasonRepository.findById(
            request.withdrawalReasonId())
        .orElseThrow(() -> new BusinessException(NOT_EXIST_WITHDRAWAL_REASON));

    MusicianWithdrawal withdrawal =
        RegisterMusicianWithdrawalRequest.toEntity(musician, withdrawalReason, request.opinion());

    // 소셜 계정만 삭제 (다시 회원가입 할 수 있도록)
    SocialAccount socialAccount = socialAccountRepository.findByMusicianId(musician.getId())
        .orElseThrow(() -> new BusinessException(SOCIAL_ACCOUNT_NOT_FOUND));
    socialAccountRepository.delete(socialAccount);

    // 탈퇴 처리
    musicianRepository.delete(musician);
    musicianRepository.flush();

    musicianWithdrawalRepository.save(withdrawal);
  }
}
