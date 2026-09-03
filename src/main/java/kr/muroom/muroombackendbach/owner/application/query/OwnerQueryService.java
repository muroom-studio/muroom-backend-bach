package kr.muroom.muroombackendbach.owner.application.query;

import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.util.PhoneNumberUtil;
import kr.muroom.muroombackendbach.owner.domain.entity.Owner;
import kr.muroom.muroombackendbach.owner.domain.repository.OwnerRepository;
import kr.muroom.muroombackendbach.owner.exception.OwnerErrorCode;
import kr.muroom.muroombackendbach.owner.presentation.assembler.OwnerAssembler;
import kr.muroom.muroombackendbach.owner.presentation.dto.response.OwnerProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerQueryService {

  private final OwnerRepository ownerRepository;
  private final OwnerAssembler ownerAssembler;

  public void isNicknameAvailable(String nickname) {
    if (ownerRepository.existsByNickname(nickname)) {
      throw new BusinessException(OwnerErrorCode.NICKNAME_ALREADY_EXISTS);
    }
  }

  public void isPhoneAvailable(String phone) {
    PhoneNumberUtil.isValidHyphenPhoneNumber(phone);

    if (ownerRepository.existsByPhoneNumber(phone)) {
      throw new BusinessException(OwnerErrorCode.PHONENUMBER_ALREADY_EXISTS);
    }
  }

  public OwnerProfileResponse getMyProfile(Long ownerId) {
    Owner owner = ownerRepository.findById(ownerId)
        .orElseThrow(() -> new BusinessException(OwnerErrorCode.OWNER_NOT_FOUND));

    return ownerAssembler.toOwnerProfileResponse(owner);
  }

}
