package kr.muroom.muroombackendbach.studioboasting.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Builder;

@Schema(description = "작업실 소개(자랑) 목록(다건) 조회 응답 DTO")
@Builder
public record StudioBoastListElementResponse(

    @Schema(description = "작업실 소개(자랑)글 ID", example = "790304842909819754", requiredMode = RequiredMode.REQUIRED)
    Long id,

    @Schema(description = "작업실 소개(자랑)글 썸네일 이미지 파일 URL",
        example = "https://muroom-storage.s3.ap-northeast-2.amazonaws.com/studio-boasting/thumbnails/thumbnail-image-01.png",
        requiredMode = RequiredMode.REQUIRED)
    String thumbnailImageFileUrl
) {

}
