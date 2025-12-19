package kr.muroom.muroombackendbach.admin.inquiry.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.muroom.muroombackendbach.admin.inquiry.presentation.dto.InquiryReplyRequest;
import kr.muroom.muroombackendbach.inquiry.application.InquiryService;
import lombok.RequiredArgsConstructor;
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

  private final InquiryService inquiryService;

  @Operation(
      summary = "1:1 문의 답글 생성",
      description = "관리자 답글 추가"
  )
  @PostMapping("/{inquiryId}")
  public void registerInquiryReply(@PathVariable Long inquiryId,
      @RequestBody InquiryReplyRequest request) {
    inquiryService.registerInquiryReply(inquiryId, request);
  }
}
