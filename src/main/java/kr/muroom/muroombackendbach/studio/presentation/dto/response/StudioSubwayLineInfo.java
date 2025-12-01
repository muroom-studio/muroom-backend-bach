package kr.muroom.muroombackendbach.studio.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "지하철 노선 정보")
public record StudioSubwayLineInfo(String lineName, String lineColor) {

}