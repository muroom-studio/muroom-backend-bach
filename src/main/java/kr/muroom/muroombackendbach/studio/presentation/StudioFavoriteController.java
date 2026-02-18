package kr.muroom.muroombackendbach.studio.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.auth.annotation.CurrentSubjectId;
import kr.muroom.muroombackendbach.auth.annotation.CurrentUserId;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.studio.application.StudioFavoriteService;
import kr.muroom.muroombackendbach.studioboasting.presentation.dto.response.StudioBoastDetailResponse.StudioInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Studio Favorite", description = "스튜디오 찜 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/studios")
public class StudioFavoriteController {

  private final StudioFavoriteService studioFavoriteService;

  @Operation(summary = "스튜디오 찜하기", description = "스튜디오를 찜합니다. (멱등)")
  @PutMapping("/{studioId}/favorite")
  public ApiResponse<Boolean> addFavorite(
      @PathVariable Long studioId,
      @Parameter(hidden = true)
      @CurrentSubjectId String subjectId
  ) {
    studioFavoriteService.addFavorite(studioId, subjectId);
    return ApiResponse.success(true);
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @Operation(summary = "스튜디오 찜 취소", description = "스튜디오 찜을 취소합니다. (멱등)")
  @DeleteMapping("/{studioId}/favorite")
  public ApiResponse<Boolean> removeFavorite(
      @PathVariable Long studioId,
      @Parameter(hidden = true)
      @CurrentSubjectId String subjectId
  ) {
    studioFavoriteService.removeFavorite(studioId, subjectId);
    return ApiResponse.success(false);
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @Operation(
      summary = "찜한 스튜디오 목록 조회",
      description = "사용자가 찜한 스튜디오 목록을 조회합니다."
  )
  @GetMapping("/favorites")
  public ApiResponse<PaginatedData<StudioInfo>> getFavoriteStudios(
      @Parameter(hidden = true)
      @CurrentSubjectId String subjectId,
      @PageableDefault Pageable pageable
  ) {
    PageImpl<StudioInfo> favorites =
        studioFavoriteService.getFavoriteStudios(subjectId, pageable);

    return ApiResponse.success(PaginatedData.from(favorites));
  }

}
