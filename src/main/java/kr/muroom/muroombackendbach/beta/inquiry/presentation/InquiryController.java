package kr.muroom.muroombackendbach.beta.inquiry.presentation;

import kr.muroom.muroombackendbach.beta.inquiry.application.InquiryService;
import kr.muroom.muroombackendbach.beta.inquiry.presentation.dto.InquiryDto;
import kr.muroom.muroombackendbach.beta.inquiry.presentation.dto.InquiryDto.GetResponse;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.common.presentation.response.PageResponse;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/beta/inquiries")
public class InquiryController {

  private final InquiryService inquiryService;

  @PostMapping
  public ApiResponse<Void> createInquiry(@Validated @RequestBody InquiryDto.CreateRequest request) {
    inquiryService.addNewInquiry(request);
    return ApiResponse.success();
  }

  @GetMapping
  public ApiResponse<PageResponse<GetResponse>> getAllInquiries(
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<GetResponse> pageResult = inquiryService.getAllInquiries(pageable);
    PageResponse<GetResponse> response = new PageResponse<>(pageResult);
    return ApiResponse.success(response);
  }
}
