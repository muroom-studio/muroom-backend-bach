package kr.muroom.muroombackendbach.beta.registration.application;

import java.util.List;
import kr.muroom.muroombackendbach.beta.registration.domain.entity.BetaIntroductoryImage;
import kr.muroom.muroombackendbach.beta.registration.domain.entity.BetaRegistration;
import kr.muroom.muroombackendbach.beta.registration.domain.repository.BetaRegistrationRepository;
import kr.muroom.muroombackendbach.beta.registration.presetation.dto.RegistrationDto;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService.PresignedPutUrlDto;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedUrlsPutResponse;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedUrlsPutResponse.PresignedUrlInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationService {

  private final BetaRegistrationRepository betaRegistrationRepository;
  private final FileStorageService fileStorageService;

  private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
      "image/jpeg",
      "image/jpg",
      "image/png",
      "image/gif",
      "image/webp",
      "application/pdf"
  );

  public GeneratePresignedUrlsPutResponse generatePresignedPutUrls(
      RegistrationDto.GeneratePresignedUrlsRequest request) {
    List<PresignedUrlInfo> presignedUrlInfos = request.fileUploadRequests().stream()
        .map((fileRequest) -> {
          validateContentType(fileRequest.contentType());

          String domain = fileRequest.type().getDomain();

          PresignedPutUrlDto presignedUrl = fileStorageService.generatePresignedPutUrl(
              fileRequest.fileName(), domain, fileRequest.contentType());

          return new PresignedUrlInfo(presignedUrl.url(), presignedUrl.fileKey());
        })
        .toList();

    return new GeneratePresignedUrlsPutResponse(presignedUrlInfos);
  }

  public void addNewRegistration(RegistrationDto.CreateRequest request) {
    BetaRegistration newRegistration = BetaRegistration.builder()
        .name(request.name())
        .phoneNumber(request.phoneNumber())
        .thirdPartyUrl(request.thirdPartyUrl())
        .agreedToPrivacy(request.agreedToPrivacy())
        .featureSuggestions(request.featureSuggestions())
        .build();

    if (request.introductoryImageFileKeys() != null && !request.introductoryImageFileKeys()
        .isEmpty()) {
      List<BetaIntroductoryImage> images = request.introductoryImageFileKeys().stream()
          .filter(imageFileKey -> !imageFileKey.isBlank())
          .map(imageFileKey -> BetaIntroductoryImage.builder()
              .fileKey(imageFileKey)
              .build()
          )
          .toList();

      images.forEach(newRegistration::addIntroductoryImage);
    }

    betaRegistrationRepository.save(newRegistration);
  }

  private void validateContentType(String contentType) {
    if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
      throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + contentType);
    }
  }

  @Transactional(readOnly = true)
  public List<RegistrationDto.GetResponse> getAllRegistrations() {
    List<BetaRegistration> registrations = betaRegistrationRepository.findAllWithImages();

    return registrations.stream()
        .map(registration -> {
          List<String> introductoryImageUrls = registration.getIntroductoryImages().stream()
              .map(image ->
                  fileStorageService.generatePresignedGetUrl(image.getFileKey()))
              .toList();

          return RegistrationDto.GetResponse.builder()
              .id(registration.getId())
              .name(registration.getName())
              .phoneNumber(registration.getPhoneNumber())
              .thirdPartyUrl(registration.getThirdPartyUrl())
              .agreedToPrivacy(registration.getAgreedToPrivacy())
              .featureSuggestions(registration.getFeatureSuggestions())
              .introductoryImageUrls(introductoryImageUrls)
              .createdAt(registration.getCreatedAt())
              .build();
        })
        .toList();
  }
}
