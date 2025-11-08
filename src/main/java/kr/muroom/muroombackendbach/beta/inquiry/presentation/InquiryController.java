package kr.muroom.muroombackendbach.beta.inquiry.presentation;

import java.util.List;
import kr.muroom.muroombackendbach.beta.inquiry.application.InquiryService;
import kr.muroom.muroombackendbach.beta.inquiry.presentation.dto.InquiryDto;
import kr.muroom.muroombackendbach.beta.inquiry.presentation.dto.InquiryDto.GetResponse;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import lombok.RequiredArgsConstructor;
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
  public ApiResponse<List<GetResponse>> getAllInquiries() {
    List<GetResponse> response = inquiryService.getAllInquiries();
    return ApiResponse.success(response);
  }
}
