package kr.muroom.muroombackendbach.inquiry.presentation;

import java.util.List;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.inquiry.application.InquiryCategoryService;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inquiry-categories")
@RequiredArgsConstructor
public class InquiryCategoryController implements InquiryCategoryControllerDocs {

  private final InquiryCategoryService inquiryCategoryService;

  @GetMapping
  public ApiResponse<List<InquiryCategoryResponse>> getAllInquiryCategories() {
    return ApiResponse.success(inquiryCategoryService.getAllInquiryCategories());
  }
}
