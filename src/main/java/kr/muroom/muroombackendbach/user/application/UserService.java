package kr.muroom.muroombackendbach.user.application;

import static kr.muroom.muroombackendbach.user.exception.UserErrorCode.PHONE_NUMBER_ALREADY_EXISTS;

import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.util.PhoneNumberUtil;
import kr.muroom.muroombackendbach.musician.domain.repository.MusicianRepository;
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
