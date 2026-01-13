package kr.muroom.muroombackendbach.inquiry.presentation;

import kr.muroom.muroombackendbach.auth.config.CurrentUserId;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.inquiry.application.InquiryService;
import kr.muroom.muroombackendbach.inquiry.presentation.docs.InquiryControllerDocs;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.InquiryImageUploadRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.RegisterInquiryRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryAllResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.SearchInquiryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController implements InquiryControllerDocs {

  private final InquiryService inquiryService;

  @PostMapping("/presigned-url")
  public ApiResponse<GeneratePresignedPutUrlResponse> generateInquiryImagePresignedUrls(
      @Validated @RequestBody InquiryImageUploadRequest request) {
    GeneratePresignedPutUrlResponse response = inquiryService.generatePresignedPutUrl(request);
    return ApiResponse.success(response);
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @GetMapping("/my")
  public ApiResponse<PaginatedData<InquiryAllResponse>> getMyInquiry(
      @CurrentUserId Long musicianId,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
  ) {
    return ApiResponse.success(
        PaginatedData.from(inquiryService.getAllMyInquiry(musicianId, pageable)));
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @GetMapping("/search")
  public ApiResponse<PaginatedData<SearchInquiryResponse>> searchInquiry(
      @CurrentUserId Long musicianId,
      @RequestParam(required = false) String keyword,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
  ) {
    Page<SearchInquiryResponse> responses = inquiryService.searchInquiry(musicianId,
        keyword, pageable);
    return ApiResponse.success(PaginatedData.from(responses));
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @PostMapping
  public ApiResponse<Void> registerInquiry(
      @CurrentUserId Long musicianId,
      @Validated @RequestBody RegisterInquiryRequest request
  ) {
    inquiryService.registerInquiry(musicianId, request);
    return ApiResponse.success();
  }

  @PreAuthorize("hasRole('MUSICIAN')")
  @GetMapping("/{inquiryId}")
  public ApiResponse<InquiryResponse> getInquiry(
      @CurrentUserId Long musicianId,
      @PathVariable Long inquiryId
  ) {
    return ApiResponse.success(inquiryService.getInquiry(musicianId, inquiryId));
  }

}
