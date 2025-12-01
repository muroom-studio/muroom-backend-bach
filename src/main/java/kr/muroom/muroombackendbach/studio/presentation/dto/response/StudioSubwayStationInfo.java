package kr.muroom.muroombackendbach.studio.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "인근 지하철역 정보")
public record StudioSubwayStationInfo(
    String stationName,
    List<StudioSubwayLineInfo> lines
) {

}