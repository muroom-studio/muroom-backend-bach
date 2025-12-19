package kr.muroom.muroombackendbach.inquiry.presentation.dto.request;

import java.util.List;

public record RegisterInquiryRequest(
    Long categoryId,
    String title,
    String content,
    List<String> imageKeys
) {

}
