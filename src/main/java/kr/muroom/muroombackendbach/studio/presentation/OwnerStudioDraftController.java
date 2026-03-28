package kr.muroom.muroombackendbach.studio.presentation;

import java.util.List;
import kr.muroom.muroombackendbach.auth.annotation.CurrentUserId;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.domain.FileStorageLocation;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.studio.application.command.StudioDraftCommandService;
import kr.muroom.muroombackendbach.studio.application.command.dto.CreateStudioDraftCommand;
import kr.muroom.muroombackendbach.studio.application.command.dto.UpdateStudioDraftCommand;
import kr.muroom.muroombackendbach.studio.application.query.StudioDraftQueryService;
import kr.muroom.muroombackendbach.studio.domain.entity.StudioDraft;
import kr.muroom.muroombackendbach.studio.domain.valueobject.StudioDraftData;
import kr.muroom.muroombackendbach.studio.presentation.docs.OwnerStudioDraftControllerDocs;
import kr.muroom.muroombackendbach.studio.presentation.dto.request.StudioDraftImageUploadRequest;
import kr.muroom.muroombackendbach.studio.presentation.dto.request.StudioDraftSaveRequest;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDraftDetailResponse;
import kr.muroom.muroombackendbach.studio.presentation.dto.response.StudioDraftListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/owner/studios/drafts")
public class OwnerStudioDraftController implements OwnerStudioDraftControllerDocs {

  private final StudioDraftCommandService studioDraftCommandService;
  private final StudioDraftQueryService studioDraftQueryService;
  private final FileStorageService fileStorageService;

  @Override
  @PostMapping("/presigned-url")
  @PreAuthorize("hasRole('OWNER')")
  public ApiResponse<GeneratePresignedPutUrlResponse> generatePresignedUrl(
      @Validated @RequestBody StudioDraftImageUploadRequest request) {
    GeneratePresignedPutUrlResponse response =
        fileStorageService.getUploadUrl(FileStorageLocation.PRIVATE_DRAFT, request);
    return ApiResponse.success(response);
  }

  @Override
  @PostMapping
  @PreAuthorize("hasRole('OWNER')")
  public ApiResponse<String> createStudioDraft(@CurrentUserId Long ownerId, @RequestBody StudioDraftSaveRequest request) {
    StudioDraftData studioDraftData = request.studioDraftData().toDomain();
    CreateStudioDraftCommand createStudioDraftCommand = CreateStudioDraftCommand.builder()
        .ownerId(ownerId)
        .step(request.step())
        .studioDraftData(studioDraftData)
        .build();

    String response = String.valueOf(studioDraftCommandService.createStudioDraft(createStudioDraftCommand));

    return ApiResponse.created(response);
  }

  @Override
  @GetMapping
  @PreAuthorize("hasRole('OWNER')")
  public ApiResponse<List<StudioDraftListResponse>> getStudioDrafts(@CurrentUserId Long ownerId) {
    List<StudioDraft> ownerStudioDrafts = studioDraftQueryService.getOwnerStudioDrafts(ownerId);

    List<StudioDraftListResponse> response = ownerStudioDrafts.stream()
        .map(StudioDraftListResponse::from)
        .toList();

    return ApiResponse.success(response);
  }

  @Override
  @GetMapping("/{studioDraftId}")
  @PreAuthorize("hasRole('OWNER')")
  public ApiResponse<StudioDraftDetailResponse> getStudioDraft(@CurrentUserId Long ownerId, @PathVariable Long studioDraftId) {
    StudioDraft studioDraft = studioDraftQueryService.getStudioDraft(ownerId, studioDraftId);

    StudioDraftDetailResponse response = StudioDraftDetailResponse.from(studioDraft);

    return ApiResponse.success(response);
  }

  @Override
  @PutMapping("/{studioDraftId}")
  @PreAuthorize("hasRole('OWNER')")
  public ApiResponse<Void> updateStudioDraft(@CurrentUserId Long ownerId, @PathVariable Long studioDraftId,
      @RequestBody StudioDraftSaveRequest request) {
    StudioDraftData studioDraftData = request.studioDraftData().toDomain();
    UpdateStudioDraftCommand updateStudioDraftCommand = UpdateStudioDraftCommand.builder()
        .ownerId(ownerId)
        .studioDraftId(studioDraftId)
        .step(request.step())
        .studioDraftData(studioDraftData)
        .build();

    studioDraftCommandService.updateStudioDraft(updateStudioDraftCommand);

    return ApiResponse.success();
  }

  @Override
  @DeleteMapping("/{studioDraftId}")
  @PreAuthorize("hasRole('OWNER')")
  public ApiResponse<Void> deleteStudioDraft(@CurrentUserId Long ownerId, @PathVariable Long studioDraftId) {
    studioDraftCommandService.deleteStudioDraft(ownerId, studioDraftId);
    return ApiResponse.deleted();
  }
}