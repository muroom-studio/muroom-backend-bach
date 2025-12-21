package kr.muroom.muroombackendbach.admin.studio.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.request.FileUploadRequest;
import kr.muroom.muroombackendbach.studio.domain.enums.StudioImageCategory;

public record StudioImagePresignedUrlRequest(
    @NotNull
    List<StudioImageUploadRequest> studioImages
) {

  private static final String DOMAIN_DIRECTORY_PREFIX = "studios/";

  public record StudioImageUploadRequest(
      @NotBlank String fileName,
      @NotBlank StudioImageCategory category,
      @NotBlank String contentType
  ) implements FileUploadRequest {

    @Override
    public String getFileName() {
      return fileName;
    }

    @Override
    public String getContentType() {
      return contentType;
    }

    @Override
    public String getDomainDirectory() {
      return DOMAIN_DIRECTORY_PREFIX + category.name().toLowerCase();
    }
  }

}
