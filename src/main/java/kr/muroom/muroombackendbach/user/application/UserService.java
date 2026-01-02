package kr.muroom.muroombackendbach.user.application;

import static kr.muroom.muroombackendbach.user.exception.UserErrorCode.*;

import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.util.PhoneNumberUtil;
import kr.muroom.muroombackendbach.user.domain.repository.MusicianRepository;
import kr.muroom.muroombackendbach.user.domain.repository.OwnerRepository;
import kr.muroom.muroombackendbach.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final MusicianRepository musicianRepository;

  public boolean isNicknameAvailable(String nickname) {
    boolean existsInMusician = musicianRepository.existsByNickname(nickname);
    return !(existsInMusician);
  }

  public void isPhoneAvailable(String phone) {
    PhoneNumberUtil.isValidHyphenPhoneNumber(phone);

    if (musicianRepository.existsByPhoneNumber(phone)) {
      throw new BusinessException(PHONE_NUMBER_ALREADY_EXISTS);
    }
  }
}
