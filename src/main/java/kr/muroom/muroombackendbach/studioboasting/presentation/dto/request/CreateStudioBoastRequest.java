package kr.muroom.muroombackendbach.studioboasting.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;

@Schema(description = "내 작업실 소개(자랑) 생성 요청 DTO")
@Builder
public record CreateStudioBoastRequest(
    @Schema(description = "내 작업실 소개(자랑) 내용", example = "우리 작업실은 최고의 시설을 갖추고 있습니다.")
    @Size(min = 10, max = 1024)
    String content,

    @Schema(description = "작업실 이름", example = "뮤룸 스튜디오", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    String studioName,

    @Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 123", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    String roadNameAddress,

    @Schema(description = "지번 주소", example = "서울특별시 강남구 역삼동 456-7", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    String lotNumberAddress,

    @Schema(description = "상세 주소", example = "3층 301호", requiredMode = RequiredMode.REQUIRED)
    @NotBlank
    String detailedAddress,

    @Schema(description = "이벤트 약관 동의 여부", example = "true", requiredMode = RequiredMode.REQUIRED)
    boolean agreedToEventTerms,

    @Schema(description = "인스타그램 계정", example = "muroom_studio", nullable = true)
    String instagramAccount,

    @Schema(description = "작업실 ID", example = "9876543210", nullable = true)
    Long studioId,

    @Schema(description = "이미지 파일 키 목록", example = "[\"image1.jpg\", \"image2.jpg\"]", requiredMode = RequiredMode.REQUIRED)
    @Size(min = 1, max = 3)
    List<String> imageFileKeys
) {

}
