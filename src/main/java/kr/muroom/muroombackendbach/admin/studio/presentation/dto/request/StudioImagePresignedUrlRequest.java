package kr.muroom.muroombackendbach.admin.studio.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import kr.muroom.muroombackendbach.studio.domain.enums.StudioImageCategory;

public record StudioImagePresignedUrlRequest(
    @NotNull
    List<StudioImageInfo> studioImages
) {

  public record StudioImageInfo(
      @NotBlank String fileName,
      @NotBlank StudioImageCategory category,
      @NotBlank String contentType
  ) {

  }

}
