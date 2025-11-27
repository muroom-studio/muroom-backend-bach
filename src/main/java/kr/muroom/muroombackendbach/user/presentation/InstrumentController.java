package kr.muroom.muroombackendbach.user.presentation;

import java.util.List;
import kr.muroom.muroombackendbach.user.application.InstrumentService;
import kr.muroom.muroombackendbach.user.presentation.dto.InstrumentDto.InstrumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/instruments")
@RequiredArgsConstructor
public class InstrumentController {

  private final InstrumentService instrumentService;

  /**
   * 악기 전체 목록 조회
   */
  @GetMapping
  public List<InstrumentResponse> getAllInstruments() {
    return instrumentService.getAllInstruments();
  }
}
