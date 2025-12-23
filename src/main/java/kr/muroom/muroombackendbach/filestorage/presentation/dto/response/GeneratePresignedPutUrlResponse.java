package kr.muroom.muroombackendbach.filestorage.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "프리사인드 업로드 URL 생성 응답 DTO")
@Builder
public record GeneratePresignedPutUrlResponse(
    @Schema(description = "프리사인드 업로드 URL", example = "https://mr-prod-public-storage.s3.ap-northeast-2.amazonaws.com/temp/...")
    String presignedPutUrl,

    @Schema(description = "파일 키", example = "temp/...")
    String fileKey
) {

}
