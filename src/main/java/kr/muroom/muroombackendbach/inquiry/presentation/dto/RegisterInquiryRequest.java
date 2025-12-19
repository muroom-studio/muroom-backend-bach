package kr.muroom.muroombackendbach.inquiry.presentation.dto;

import java.util.List;

public record RegisterInquiryRequest(
    Long categoryId,
    String title,
    String content,
    List<String> imagesKey
) {

}
