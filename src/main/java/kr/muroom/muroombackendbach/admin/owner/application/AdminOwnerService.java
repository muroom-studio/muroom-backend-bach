package kr.muroom.muroombackendbach.admin.owner.application;

import kr.muroom.muroombackendbach.admin.owner.presentation.request.OwnerCreateRequest;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.user.application.UserService;
import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import kr.muroom.muroombackendbach.user.domain.entity.UserStatus;
import kr.muroom.muroombackendbach.user.domain.repository.OwnerRepository;
import kr.muroom.muroombackendbach.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminOwnerService {

  private final OwnerRepository ownerRepository;
  private final UserService userService;
  private final UniqueNicknameCodeGenerator uniqueNicknameCodeGenerator;

  private static final String OWNER_NICKNAME_PREFIX = "사장님";

  public String generateOwnerUniqueNickname() {
    // 1. 번호표(Sequence) 하나 소모 (동시성 문제 없이 유니크함 보장)
    Long nextSeq = ownerRepository.getNextNicknameSequence();

    // 2. 번호표를 닉네임으로 변환하여 반환
    return uniqueNicknameCodeGenerator.generate(nextSeq);
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
