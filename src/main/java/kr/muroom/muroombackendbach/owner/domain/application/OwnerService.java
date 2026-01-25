package kr.muroom.muroombackendbach.owner.domain.application;

import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.owner.domain.entity.Owner;
import kr.muroom.muroombackendbach.owner.domain.repository.OwnerRepository;
import kr.muroom.muroombackendbach.owner.exception.OwnerErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

  private final OwnerRepository ownerRepository;

  public Owner findByIdOrThrowException(Long ownerId) {
    return ownerRepository.findById(ownerId)
        .orElseThrow(() -> new BusinessException(OwnerErrorCode.OWNER_NOT_FOUND));
  }

  public Owner findByPhoneNumberOrThrowException(String phoneNumber) {
    return ownerRepository.findByPhoneNumber(phoneNumber)
        .orElseThrow(() -> new BusinessException(OwnerErrorCode.OWNER_NOT_FOUND));
  }
}
