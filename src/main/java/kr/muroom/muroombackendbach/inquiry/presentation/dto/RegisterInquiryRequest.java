package kr.muroom.muroombackendbach.inquiry.presentation.dto;

import java.util.List;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryStatus;

public record RegisterInquiryRequest(
    Long categoryId,
    String title,
    String content,
    InquiryStatus status,
    List<String> imagesKey
) {
    
}
