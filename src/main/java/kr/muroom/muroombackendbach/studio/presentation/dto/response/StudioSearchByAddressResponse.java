package kr.muroom.muroombackendbach.studio.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "스튜디오 주소 검색 요청 DTO")
@Builder
public record StudioSearchByAddressResponse(
    @Schema(description = "스튜디오 ID", example = "791543436721219205")
    String id,

    @Schema(description = "스튜디오 이름", example = "뮤룸 스튜디오")
    String name,

    @Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 123")
    String roadNameAddress,

    @Schema(description = "지번 주소", example = "서울특별시 강남구 역삼동 456-7")
    String lotNumberAddress,

    @Schema(description = "상세 주소", example = "지하 1층")
    String detailedAddress
) {

}
