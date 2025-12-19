package kr.muroom.muroombackendbach.admin.inquiry.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.admin.inquiry.presentation.dto.InquiryReplyImagePresignedUrlRequest;
import kr.muroom.muroombackendbach.admin.inquiry.presentation.dto.InquiryReplyRequest;
import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedUrlsPutResponse;
import kr.muroom.muroombackendbach.inquiry.application.InquiryReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "관리자용 1:1 문의 API")
@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

  private final InquiryReplyService inquiryReplyService;

  @PostMapping("/presigned-url")
  public ApiResponse<GeneratePresignedUrlsPutResponse> generateInquiryReplyImagePresignedUrls(
      @Validated @RequestBody InquiryReplyImagePresignedUrlRequest request) {
    GeneratePresignedUrlsPutResponse response = inquiryReplyService.generatePresignedPutUrls(
        request);
    return ApiResponse.success(response);
  }

  @Operation(
      summary = "1:1 문의 답글 생성",
      description = "관리자 답글 추가"
  )
  @PostMapping("/{inquiryId}/reply")
  public void registerInquiryReply(@PathVariable Long inquiryId,
      @RequestBody InquiryReplyRequest request) {
    inquiryReplyService.registerInquiryReply(inquiryId, request);
  }
}
