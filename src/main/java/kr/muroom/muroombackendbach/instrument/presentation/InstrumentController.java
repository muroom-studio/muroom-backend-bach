package kr.muroom.muroombackendbach.instrument.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.instrument.application.InstrumentService;
import kr.muroom.muroombackendbach.instrument.presentation.dto.InstrumentDto.InstrumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "instrument - 악기 종류 API")
@RestController
@RequestMapping("/api/v1/instruments")
@RequiredArgsConstructor
public class InstrumentController {

  private final InstrumentService instrumentService;

  /**
   * 악기 전체 목록 조회
   */
  @Operation(
      summary = "전체 악기 조회",
      description = "악기 리스트 조회"
  )
  @GetMapping
  public ApiResponse<List<InstrumentResponse>> getAllInstruments() {
    return ApiResponse.success(instrumentService.getAllInstruments());
  }
}
