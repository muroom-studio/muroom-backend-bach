package kr.muroom.muroombackendbach.user.application;

import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.user.domain.repository.MusicianRepository;
import kr.muroom.muroombackendbach.user.domain.repository.OwnerRepository;
import kr.muroom.muroombackendbach.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final MusicianRepository musicianRepository;
  private final OwnerRepository ownerRepository;

  public boolean isNicknameAvailable(String nickname) {
    boolean existsInMusician = musicianRepository.existsByNickname(nickname);
    return !(existsInMusician);
  }

  public void isPhoneAvailable(String phone) {
    if (musicianRepository.existsByPhoneNumber(phone)) {
      throw new BusinessException(UserErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }

  public boolean isExistingMusicianId(Long musicianId) {
    return musicianRepository.existsById(musicianId);
  }
}
