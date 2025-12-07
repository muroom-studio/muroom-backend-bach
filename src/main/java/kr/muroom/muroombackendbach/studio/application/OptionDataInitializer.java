package kr.muroom.muroombackendbach.studio.application;

import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.entity.Option;
import kr.muroom.muroombackendbach.studio.domain.enums.OptionCategory;
import kr.muroom.muroombackendbach.studio.domain.repository.OptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("!test")
@Slf4j
@Component
@RequiredArgsConstructor
public class OptionDataInitializer implements ApplicationRunner {

  private final OptionRepository optionRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (optionRepository.count() > 0) {
      log.info("Option 데이터가 이미 존재하므로 초기화를 건너뜁니다.");
      return;
    }

    log.info("Option 데이터 초기화를 시작합니다.");

    List<Option> options = List.of(
        // COMMON Options
        Option.builder().category(OptionCategory.COMMON).code("WATER_PURIFIER")
            .description("정수기").iconImageKey("/systems/icons/WATER_PURIFIER.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("MICROWAVE").description("전자레인지")
            .iconImageKey("/systems/icons/MICROWAVE.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("WASHING_MACHINE")
            .description("세탁기").iconImageKey("/systems/icons/WASHING_MACHINE.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("DRYER").description("건조기")
            .iconImageKey("/systems/icons/DRYER.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("COFFEE_MACHINE")
            .description("커피머신").iconImageKey("/systems/icons/COFFEE_MACHINE.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("PRINTER").description("프린터기")
            .iconImageKey("/systems/icons/PRINTER.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("VENTILATION_SYSTEM")
            .description("환기시스템").iconImageKey("/systems/icons/VENTILATION_SYSTEM.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("SHOWER_ROOM").description("샤워실")
            .iconImageKey("/systems/icons/SHOWER_ROOM.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("CCTV").description("CCTV")
            .iconImageKey("/systems/icons/CCTV.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("REFRIGERATOR").description("냉장고")
            .iconImageKey("/systems/icons/REFRIGERATOR.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("SNACKS").description("간식")
            .iconImageKey("/systems/icons/SNACKS.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("SHOE_RACK").description("신발장")
            .iconImageKey("/systems/icons/SHOE_RACK.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("AIR_FRYER").description("에어프라이기")
            .iconImageKey("/systems/icons/AIR_FRYER.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("BIDET").description("비데")
            .iconImageKey("/systems/icons/BIDET.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("FOOD_WASTE_DISPOSER")
            .description("음식물처리기").iconImageKey("/systems/icons/FOOD_WASTE_DISPOSER.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("WIFI").description("와이파이")
            .iconImageKey("/systems/icons/WIFI.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("BUSINESS_INTERNET").description("기업인터넷")
            .iconImageKey("/systems/icons/BUSINESS_INTERNET.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("SINK").description("싱크대")
            .iconImageKey("/systems/icons/SINK.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("LOUNGE").description("휴게실")
            .iconImageKey("/systems/icons/LOUNGE.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("FIRE_DETECTOR").description("화재감지기")
            .iconImageKey("/systems/icons/FIRE_DETECTOR.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("FIRE_EXTINGUISHER").description("소화기")
            .iconImageKey("/systems/icons/FIRE_EXTINGUISHER.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("LOCKER_ROOM").description("락커룸")
            .iconImageKey("/systems/icons/LOCKER_ROOM.svg").build(),
        Option.builder().category(OptionCategory.COMMON).code("PC").description("PC")
            .iconImageKey("/systems/icons/PC.svg").build(),

        // INDIVIDUAL Options
        Option.builder().category(OptionCategory.INDIVIDUAL).code("LAN_PORT").description("LAN 포트")
            .iconImageKey("/systems/icons/LAN_PORT.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("WINDOW").description("창문")
            .iconImageKey("/systems/icons/WINDOW.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("FLOOR_HEATING")
            .description("바닥난방").iconImageKey("/systems/icons/FLOOR_HEATING.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("AIR_PURIFIER")
            .description("공기청정기").iconImageKey("/systems/icons/AIR_PURIFIER.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("DEHUMIDIFIER")
            .description("제습기").iconImageKey("/systems/icons/DEHUMIDIFIER.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("RADIATOR").description("라디에이터")
            .iconImageKey("/systems/icons/RADIATOR.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("AIR_CONDITIONER")
            .description("에어컨").iconImageKey("/systems/icons/AIR_CONDITIONER.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("FULL_LENGTH_MIRROR")
            .description("전신거울").iconImageKey("/systems/icons/FULL_LENGTH_MIRROR.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("LIGHTING").description("조명")
            .iconImageKey("/systems/icons/LIGHTING.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("LOCKER").description("보관함")
            .iconImageKey("/systems/icons/LOCKER.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("DRUMS").description("드럼")
            .iconImageKey("/systems/icons/DRUMS.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("DOOR_LOCK").description("도어락")
            .iconImageKey("/systems/icons/DOOR_LOCK.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("KEYBOARD").description("키보드")
            .iconImageKey("/systems/icons/KEYBOARD.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("PIANO").description("피아노")
            .iconImageKey("/systems/icons/PIANO.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("DOUBLE_SYSTEM_DOOR")
            .description("2중시스템도어").iconImageKey("/systems/icons/DOUBLE_SYSTEM_DOOR.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("AMPLIFIER").description("앰프")
            .iconImageKey("/systems/icons/AMPLIFIER.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("DESK").description("책상")
            .iconImageKey("/systems/icons/DESK.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("CHAIR").description("의자")
            .iconImageKey("/systems/icons/CHAIR.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("MIXER").description("믹서")
            .iconImageKey("/systems/icons/MIXER.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("AUDIO_CABLE").description("오디오배선")
            .iconImageKey("/systems/icons/AUDIO_CABLE.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("AUDIO_INTERFACE").description("오인페")
            .iconImageKey("/systems/icons/AUDIO_INTERFACE.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("MUSIC_STAND").description("보면대")
            .iconImageKey("/systems/icons/MUSIC_STAND.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("SPEAKER").description("오디오")
            .iconImageKey("/systems/icons/SPEAKER.svg").build(),
        Option.builder().category(OptionCategory.INDIVIDUAL).code("MICROPHONE").description("마이크")
            .iconImageKey("/systems/icons/MICROPHONE.svg").build(),

        // ETC
        Option.builder().category(OptionCategory.ETC).code("NONE").description("없음")
            .iconImageKey("/systems/icons/NONE.svg").build()
    );

    optionRepository.saveAll(options);
    log.info("Option 데이터 초기화를 완료했습니다.");
  }
}
