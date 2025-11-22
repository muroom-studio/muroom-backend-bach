package kr.muroom.muroombackendbach.beta.inquiry.presentation;

import kr.muroom.muroombackendbach.beta.inquiry.application.InquiryService;
import kr.muroom.muroombackendbach.beta.inquiry.presentation.dto.InquiryDto;
import kr.muroom.muroombackendbach.beta.inquiry.presentation.dto.InquiryDto.GetResponse;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PaginatedData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 문의사항 관련 API를 처리하는 컨트롤러 클래스입니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/beta/inquiries")
public class InquiryController {

  private final InquiryService inquiryService;

  /**
   * 새로운 문의사항을 생성합니다.
   *
   * @param request 문의사항 생성 요청 데이터
   * @return 성공 응답
   */
  @PostMapping
  public ApiResponse<Void> createInquiry(@Validated @RequestBody InquiryDto.CreateRequest request) {
    inquiryService.addNewInquiry(request);
    return ApiResponse.success();
  }

  /**
   * 모든 문의사항을 페이지네이션하여 조회합니다.
   *
   * @param pageable 페이지네이션 정보
   * @return 페이지네이션된 문의사항 목록 응답
   */
  @GetMapping
  public ApiResponse<PaginatedData<GetResponse>> getAllInquiries(
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<GetResponse> inquiries = inquiryService.getAllInquiries(pageable);
    PaginatedData<GetResponse> response = PaginatedData.from(inquiries);
    return ApiResponse.success(response);
  }
}
