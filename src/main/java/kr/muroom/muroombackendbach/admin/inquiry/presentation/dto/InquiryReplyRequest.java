package kr.muroom.muroombackendbach.admin.inquiry.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record InquiryReplyRequest(

    @Schema(example = "뮤지션님 ~ ")
    @NotBlank
    String content,
    
    List<String> imageKeys
) {

}
