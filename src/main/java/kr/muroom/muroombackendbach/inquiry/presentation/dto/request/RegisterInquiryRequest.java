package kr.muroom.muroombackendbach.inquiry.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RegisterInquiryRequest(
    @NotNull
    Long categoryId,
    @NotBlank
    String title,
    String content,
    List<String> imageKeys
) {

}
