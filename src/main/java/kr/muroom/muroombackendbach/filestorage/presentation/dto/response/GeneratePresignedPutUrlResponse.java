package kr.muroom.muroombackendbach.filestorage.presentation.dto.response;

import lombok.Builder;

@Builder
public record GeneratePresignedPutUrlResponse(
    String presignedPutUrl,
    String fileKey
) {

}
