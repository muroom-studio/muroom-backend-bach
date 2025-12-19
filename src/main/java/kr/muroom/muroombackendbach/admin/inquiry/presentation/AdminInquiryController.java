package kr.muroom.muroombackendbach.admin.inquiry.presentation;

import kr.muroom.muroombackendbach.admin.inquiry.presentation.dto.InquiryReplyRequest;
import kr.muroom.muroombackendbach.inquiry.application.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

  private final InquiryService inquiryService;

  @PostMapping("/{inquiryId}")
  public void registerInquiryReply(@PathVariable Long inquiryId,
      @RequestBody InquiryReplyRequest request) {
    inquiryService.registerInquiryReply(inquiryId, request);
  }
}
