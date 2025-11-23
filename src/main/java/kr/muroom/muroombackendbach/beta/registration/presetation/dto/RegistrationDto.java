package kr.muroom.muroombackendbach.beta.registration.presetation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.request.FileUploadRequest;
import lombok.Builder;

public final class RegistrationDto {

  private RegistrationDto() {
  }

  @Builder
  public record GeneratePresignedUrlsRequest(
      @NotEmpty @Valid @Size(max = 10, message = "파일 업로드 요청은 최대 10개까지 가능합니다.")
      List<FileUploadRequest> fileUploadRequests
  ) {

  }

  @Builder
  public record CreateRequest(
      @NotBlank(message = "이름은 필수 입력입니다.") String name,
      @NotBlank(message = "전화번호는 필수 입력입니다.") String phoneNumber,
      @NotBlank(message = "외부 URL은 필수 입력입니다.") String thirdPartyUrl,
      @AssertTrue(message = "개인정보 수집 및 이용에 동의하셔야 합니다.") Boolean agreedToPersonalInfoCollection,
      @AssertTrue(message = "작업실 콘텐츠 수집에 동의하셔야 합니다.")
      Boolean agreedToContentCollection,
      @AssertTrue(message = "제3자 제공에 동의하셔야 합니다.") Boolean agreedToThirdPartyProvision,
      Boolean agreedToMarketing,
      String featureSuggestions,
      @Size(max = 10, message = "소개 이미지 파일은 최대 10개까지 업로드할 수 있습니다.")
      List<String> introductoryImageFileKeys
  ) {

  }

  @Builder
  public record GetResponse(
      Long id,
      String name,
      String phoneNumber,
      String thirdPartyUrl,
      Boolean agreedToPersonalInfoCollection,
      Boolean agreedToContentCollection,
      Boolean agreedToThirdPartyProvision,
      Boolean agreedToMarketing,
      String featureSuggestions,
      List<String> introductoryImageUrls,
      LocalDateTime createdAt
  ) {

  }

  @Builder
  public record CountResponse(
      Long totalRegistrations
  ) {

  }
}
