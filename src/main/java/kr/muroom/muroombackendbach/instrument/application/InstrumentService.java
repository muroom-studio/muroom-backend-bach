package kr.muroom.muroombackendbach.instrument.application;

import java.util.List;
import kr.muroom.muroombackendbach.instrument.domain.entity.Instrument;
import kr.muroom.muroombackendbach.instrument.domain.repository.InstrumentRepository;
import kr.muroom.muroombackendbach.instrument.presentation.dto.InstrumentDto.InstrumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InstrumentService {

  private final InstrumentRepository instrumentRepository;

  public List<InstrumentResponse> getAllInstruments() {
    List<Instrument> instruments = instrumentRepository.findAll();

    return instruments.stream()
        .map(InstrumentResponse::from)
        .toList();
  }
}
