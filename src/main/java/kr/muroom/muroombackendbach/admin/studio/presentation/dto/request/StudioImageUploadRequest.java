package kr.muroom.muroombackendbach.admin.studio.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.request.FileUploadRequest;
import kr.muroom.muroombackendbach.studio.domain.enums.StudioImageCategory;

public record StudioImageUploadRequest(
    @NotBlank String fileName,
    @NotNull StudioImageCategory category,
    @NotBlank String contentType
) implements FileUploadRequest {

  private static final String DOMAIN_DIRECTORY_PREFIX = "studios";

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
