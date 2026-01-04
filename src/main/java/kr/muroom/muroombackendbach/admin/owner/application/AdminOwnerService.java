package kr.muroom.muroombackendbach.admin.owner.application;

import java.security.SecureRandom;
import kr.muroom.muroombackendbach.admin.owner.presentation.request.OwnerCreateRequest;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.owner.domain.entity.Owner;
import kr.muroom.muroombackendbach.owner.domain.repository.OwnerRepository;
import kr.muroom.muroombackendbach.musician.domain.entity.UserStatus;
import kr.muroom.muroombackendbach.musician.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminOwnerService {

  private final OwnerRepository ownerRepository;
  private final UniqueNicknameCodeGenerator uniqueNicknameCodeGenerator;
  private static final SecureRandom RND = new SecureRandom();

  private static final int MAX_ATTEMPTS = 30;
  private static final int SPACE = 1_000_000;

  @Transactional(readOnly = true)
  public String generateOwnerUniqueNickname() {
    for (int i = 0; i < MAX_ATTEMPTS; i++) {
      long candidate = RND.nextInt(SPACE); // 0..999999

      String nickname = uniqueNicknameCodeGenerator.generate(candidate);

      // 이미 쓰는 닉네임이면 다시
      if (!ownerRepository.existsByNickname(nickname)) {
        return nickname;
      }
    }

    throw new IllegalStateException("닉네임 후보 생성에 실패했습니다. (재시도 초과)");
  }

  public void createOwner(OwnerCreateRequest request) {
    Boolean existsByPhoneNumber = ownerRepository.existsByPhoneNumber(request.phoneNumber());
    if (existsByPhoneNumber) {
      throw new BusinessException(UserErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
    }

    Owner newOwner = Owner.builder()
        .nickname(request.nickname())
        .phoneNumber(request.phoneNumber())
        .experienceYears(request.experienceYears())
        .status(UserStatus.UNVERIFIED)
        .build();

    ownerRepository.save(newOwner);
  }
}
