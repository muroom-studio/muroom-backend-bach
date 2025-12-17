package kr.muroom.muroombackendbach.inquiry.presentation;

import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.inquiry.application.InquiryService;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.InquiryAllResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.InquiryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

  private final InquiryService inquiryService;

  @GetMapping("/my")
  public ApiResponse<List<InquiryAllResponse>> getMyInquiry(
      @AuthenticationPrincipal Long musicianId
  ) {
    return ApiResponse.success(inquiryService.getMyInquiry(musicianId));
  }

  @GetMapping("{inquiryId}")
  public ApiResponse<InquiryResponse> getInquiry(
      @AuthenticationPrincipal Long musicianId,
      @PathVariable Long inquiryId
  ) {
    return ApiResponse.success(inquiryService.getInquiry(musicianId, inquiryId));
  }

}
