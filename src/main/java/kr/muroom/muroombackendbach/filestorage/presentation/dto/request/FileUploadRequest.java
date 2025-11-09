package kr.muroom.muroombackendbach.filestorage.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.muroom.muroombackendbach.common.domain.FileUploadType;

public record FileUploadRequest(
    @NotNull FileUploadType type,
    @NotBlank String fileName,
    @NotBlank String contentType
) {

}