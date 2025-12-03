package kr.muroom.muroombackendbach.instrument.application;

import java.util.List;
import kr.muroom.muroombackendbach.user.domain.entity.Instrument;
import kr.muroom.muroombackendbach.user.domain.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class InstrumentDataInitializer implements ApplicationRunner {

  private final InstrumentRepository instrumentRepository;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (instrumentRepository.count() > 0) {
      log.info("Instrument 데이터가 이미 존재하므로 초기화를 건너뜁니다.");
      return;
    }

    log.info("Instrument 데이터 초기화를 시작합니다.");

    List<Instrument> instruments = List.of(
        Instrument.builder().code("VOCAL").description("보컬").build(),
        Instrument.builder().code("GUITAR").description("기타").build(),
        Instrument.builder().code("BASS").description("베이스").build(),
        Instrument.builder().code("KEYBOARD").description("키보드").build(),
        Instrument.builder().code("PIANO").description("피아노").build(),
        Instrument.builder().code("DRUM").description("드럼").build(),
        Instrument.builder().code("MIDI").description("MIDI").build(),
        Instrument.builder().code("BRASS_WIND").description("금관").build(),
        Instrument.builder().code("WOOD_WIND").description("목관").build(),
        Instrument.builder().code("STRINGS").description("현악").build(),
        Instrument.builder().code("VOCAL_PERFORMANCE").description("성악").build(),
        Instrument.builder().code("KR_TRADITIONAL").description("국악").build(),
        Instrument.builder().code("ETC").description("그 외").build()
    );

    instrumentRepository.saveAll(instruments);
    log.info("Instrument 데이터 초기화를 완료했습니다.");
  }
}