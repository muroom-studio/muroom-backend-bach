package kr.muroom.muroombackendbach.inquiry.presentation;

import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedUrlsPutResponse;
import kr.muroom.muroombackendbach.inquiry.application.InquiryService;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.InquiryImagePresignedUrlRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.SearchInquiryRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryAllResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.RegisterInquiryRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.SearchInquiryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController implements InquiryControllerDocs {

  private final InquiryService inquiryService;

  @PostMapping("/presigned-url")
  public ApiResponse<GeneratePresignedUrlsPutResponse> generateInquiryImagePresignedUrls(
      @Validated @RequestBody InquiryImagePresignedUrlRequest request) {
    GeneratePresignedUrlsPutResponse response = inquiryService.generatePresignedPutUrls(
        request);
    return ApiResponse.success(response);
  }

  @GetMapping("/my")
  public ApiResponse<PaginatedData<InquiryAllResponse>> getMyInquiry(
      @AuthenticationPrincipal Long musicianId,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
  ) {
    return ApiResponse.success(
        PaginatedData.from(inquiryService.getAllMyInquiry(musicianId, pageable)));
  }

  @PostMapping("/search")
  public ApiResponse<PaginatedData<SearchInquiryResponse>> searchInquiry(
      @AuthenticationPrincipal Long musicianId,
      @RequestBody SearchInquiryRequest searchInquiryRequest,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
  ) {
    Page<SearchInquiryResponse> responses = inquiryService.searchInquiry(musicianId,
        searchInquiryRequest, pageable);
    return ApiResponse.success(PaginatedData.from(responses));
  }

  @PostMapping
  public void registerInquiry(
      @AuthenticationPrincipal Long musicianId,
      @RequestBody RegisterInquiryRequest request) {
    inquiryService.registerInquiry(musicianId, request);
  }

  @GetMapping("/{inquiryId}")
  public ApiResponse<InquiryResponse> getInquiry(
      @AuthenticationPrincipal Long musicianId,
      @PathVariable Long inquiryId
  ) {
    return ApiResponse.success(inquiryService.getInquiry(musicianId, inquiryId));
  }

}
