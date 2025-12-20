package kr.muroom.muroombackendbach.admin.inquiry.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record UpdateInquiryReplyRequest(
    List<String> inquiryReplyImages,
    @NotBlank String content
) {

}
