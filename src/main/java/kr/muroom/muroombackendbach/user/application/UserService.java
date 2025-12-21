package kr.muroom.muroombackendbach.user.application;

import kr.muroom.muroombackendbach.user.domain.repository.MusicianRepository;
import kr.muroom.muroombackendbach.user.domain.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final MusicianRepository musicianRepository;
  private final OwnerRepository ownerRepository;

  public boolean isNicknameAvailable(String nickname) {
    boolean existsInMusician = musicianRepository.existsByNickname(nickname);
    boolean existsInOwner = ownerRepository.existsByNickname(nickname);
    return !(existsInMusician || existsInOwner);
  }
  
  public boolean isValidMusicianId(Long musicianId) {
    return musicianRepository.existsById(musicianId);
  }
}
