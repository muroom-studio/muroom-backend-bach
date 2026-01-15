package kr.muroom.muroombackendbach.musician.presentation.assembler;

import kr.muroom.muroombackendbach.auth.auth.domain.entity.SocialAccount;
import kr.muroom.muroombackendbach.instrument.domain.entity.Instrument;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import kr.muroom.muroombackendbach.musician.domain.entity.MyStudio;
import kr.muroom.muroombackendbach.musician.domain.entity.UserStatus;
import kr.muroom.muroombackendbach.musician.presentation.dto.request.MusicianSignupRequest;
import kr.muroom.muroombackendbach.musician.presentation.dto.response.MusicianProfileResponse;
import kr.muroom.muroombackendbach.musician.presentation.dto.response.MusicianProfileResponse.MyStudioInfo;
import kr.muroom.muroombackendbach.musician.presentation.dto.response.MusicianSimpleProfileResponse;
import kr.muroom.muroombackendbach.musician.presentation.dto.response.MusicianSimpleProfileResponse.InstrumentSimpleInfo;
import org.springframework.stereotype.Component;

@Component
public class MusicianAssembler {

  public Musician toNewMusician(
      MusicianSignupRequest request,
      String phone,
      Instrument instrument
  ) {
    return Musician.builder()
        .name(request.name())
        .phoneNumber(phone)
        .nickname(request.nickname())
        .status(UserStatus.ACTIVE)
        .instrument(instrument)
        .build();
  }

  public MusicianProfileResponse toMusicianProfileResponse(
      Musician musician,
      SocialAccount socialAccount,
      MyStudio myStudio
  ) {
    return MusicianProfileResponse.builder()
        .musicianId(String.valueOf(musician.getId()))
        .phone(musician.getPhoneNumber())
        .nickname(musician.getNickname())
        .musicianInstrument(toInstrumentSimpleInfo(musician.getInstrument()))
        .snsAccount(socialAccount.getProvider())
        .myStudio(toMyStudioInfo(myStudio))
        .build();
  }

  public MusicianSimpleProfileResponse toMusicianSimpleProfileResponse(Musician musician) {
    return MusicianSimpleProfileResponse.builder()
        .musicianId(String.valueOf(musician.getId()))
        .nickname(musician.getNickname())
        .musicianInstrument(toInstrumentSimpleInfo(musician.getInstrument()))
        .build();
  }

  private InstrumentSimpleInfo toInstrumentSimpleInfo(Instrument instrument) {
    return InstrumentSimpleInfo.builder()
        .code(instrument.getCode())
        .description(instrument.getDescription())
        .build();
  }

  private MyStudioInfo toMyStudioInfo(MyStudio myStudio) {
    return MyStudioInfo.builder()
        .name(myStudio.getName())
        .roadAddress(myStudio.getRoadAddress())
        .detailAddress(myStudio.getDetailAddress())
        .build();
  }
}
