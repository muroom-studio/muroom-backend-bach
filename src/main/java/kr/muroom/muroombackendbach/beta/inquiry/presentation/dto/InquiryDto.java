package kr.muroom.muroombackendbach.beta.inquiry.presentation.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Builder;

public final class InquiryDto {

  private InquiryDto() {
  }

  @Builder
  public record CreateRequest(
      @NotBlank(message = "이름은 필수 입력입니다.") String name,
      @NotBlank(message = "전화번호는 필수 입력입니다.") String phoneNumber,
      @NotBlank(message = "문의 내용은 필수 입력입니다.") String content,
      @AssertTrue(message = "개인정보 수집 및 이용에 동의하셔야 합니다.") Boolean agreedToPrivacy
  ) {

  }

  @Builder
  public record GetResponse(
      Long id,
      String name,
      String phoneNumber,
      String content,
      Boolean agreedToPrivacy,
      LocalDateTime createdAt
  ) {

  }
}