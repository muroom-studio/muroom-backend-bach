package kr.muroom.muroombackendbach.withdrawal.application;

import static kr.muroom.muroombackendbach.auth.auth.exception.SocialAccountErrorCode.SOCIAL_ACCOUNT_NOT_FOUND;
import static kr.muroom.muroombackendbach.musician.exception.MusicianErrorCode.MUSICIAN_NOT_FOUND;
import static kr.muroom.muroombackendbach.withdrawal.exception.WithdrawalReasonErrorCode.NOT_EXIST_WITHDRAWAL_REASON;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import kr.muroom.muroombackendbach.auth.auth.domain.entity.SocialAccount;
import kr.muroom.muroombackendbach.auth.auth.domain.repository.SocialAccountRepository;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import kr.muroom.muroombackendbach.musician.domain.repository.MusicianRepository;
import kr.muroom.muroombackendbach.studio.domain.repository.StudioRepository;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastCommentRepository;
import kr.muroom.muroombackendbach.studioboasting.domain.repository.StudioBoastRepository;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.MusicianWithdrawal;
import kr.muroom.muroombackendbach.withdrawal.domain.entity.WithdrawalReason;
import kr.muroom.muroombackendbach.withdrawal.domain.repository.MusicianWithdrawalRepository;
import kr.muroom.muroombackendbach.withdrawal.domain.repository.WithdrawalReasonRepository;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.WithdrawalAssembler;
import kr.muroom.muroombackendbach.withdrawal.presentation.dto.request.RegisterMusicianWithdrawalRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MusicianWithdrawalService {

  private static final long HARD_DELETE_DAYS_WITH_ACTIVITY = 90;
  private static final long HARD_DELETE_DAYS_WITHOUT_ACTIVITY = 7;

  private final MusicianWithdrawalRepository musicianWithdrawalRepository;
  private final WithdrawalReasonRepository withdrawalReasonRepository;
  private final SocialAccountRepository socialAccountRepository;
  private final MusicianRepository musicianRepository;
  private final WithdrawalAssembler withdrawalAssembler;

  private final StudioBoastRepository studioBoastRepository;
  private final StudioBoastCommentRepository studioBoastCommentRepository;

  @Transactional
  public void register(Long musicianId, RegisterMusicianWithdrawalRequest request) {
    Musician musician = musicianRepository.findById(musicianId)
        .orElseThrow(() -> new BusinessException(MUSICIAN_NOT_FOUND));

    WithdrawalReason withdrawalReason = withdrawalReasonRepository.findById(
            request.withdrawalReasonId())
        .orElseThrow(() -> new BusinessException(NOT_EXIST_WITHDRAWAL_REASON));

    // 탈퇴 사유 기록 엔티티 생성
    MusicianWithdrawal withdrawal = withdrawalAssembler.toRegisterMusicianWithdrawal(
        musician, withdrawalReason, request.opinion());

    // 재가입 가능하도록 소셜 계정만 삭제
    SocialAccount socialAccount = socialAccountRepository.findByMusicianId(musician.getId())
        .orElseThrow(() -> new BusinessException(SOCIAL_ACCOUNT_NOT_FOUND));
    socialAccountRepository.delete(socialAccount);

    // hard delete 예정일 계산 및 저장 (delete 호출 전에 세팅)
    OffsetDateTime now = OffsetDateTime.now();
    OffsetDateTime hardDeleteAt = calculateHardDeleteAt(musicianId, now);
    musician.scheduleHardDeleteAt(hardDeleteAt);

    // soft delete 트리거 (@SQLDelete)
    musicianRepository.delete(musician);
    musicianRepository.flush(); // flush 하지않으면 @SQLDelete가 인식하지 못함.
    musicianWithdrawalRepository.save(withdrawal);
  }

  private OffsetDateTime calculateHardDeleteAt(Long musicianId, OffsetDateTime now) {
    return now.plusDays(hasAnyActivity(musicianId)
        ? HARD_DELETE_DAYS_WITH_ACTIVITY
        : HARD_DELETE_DAYS_WITHOUT_ACTIVITY);
  }

  private boolean hasAnyActivity(Long musicianId) {
    return studioBoastRepository.existsByCreatorUserId(musicianId)
        || studioBoastCommentRepository.existsByCreatorUserId(musicianId);
  }
}
