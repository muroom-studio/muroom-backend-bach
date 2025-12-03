package kr.muroom.muroombackendbach.admin.owner.application;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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

  private static final List<String> ADJECTIVES = List.of(
      "행복한", "즐거운", "빛나는", "용감한", "슬기로운", "신비로운",
      "엉뚱한", "씩씩한", "친절한", "고요한", "날으는", "수줍은",
      "호기심많은", "명랑한", "산책하는", "생각하는", "꿈꾸는", "기분좋은",
      "동글동글", "따뜻한", "똑똑한", "멋있는",
      "반짝이는", "상큼한", "성실한", "솔직한", "신나는", "여유로운",
      "자유로운", "작은", "조용한", "책읽는", "총명한",
      "춤추는", "포근한", "다정한"
  );

  private static final String NICKNAME_POSTFIX = "뮤즈";

  public String generateOwnerUniqueNickname() {
    return generateRandomNickname();
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

  private String generateRandomNickname() {
    String nickname;

    do {
      String adjective = ADJECTIVES.get(ThreadLocalRandom.current().nextInt(ADJECTIVES.size()));
      int random4DigitNumber = 100000 + ThreadLocalRandom.current().nextInt(900000);
      nickname = adjective + " " + NICKNAME_POSTFIX + " " + random4DigitNumber;
    } while (!userService.isNicknameAvailable(nickname));

    return nickname;
  }

}
