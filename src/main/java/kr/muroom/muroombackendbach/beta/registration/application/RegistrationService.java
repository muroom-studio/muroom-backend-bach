package kr.muroom.muroombackendbach.beta.registration.application;

import java.util.List;
import kr.muroom.muroombackendbach.beta.registration.domain.entity.BetaIntroductoryImage;
import kr.muroom.muroombackendbach.beta.registration.domain.entity.BetaRegistration;
import kr.muroom.muroombackendbach.beta.registration.domain.repository.BetaRegistrationRepository;
import kr.muroom.muroombackendbach.beta.registration.presetation.dto.RegistrationDto;
import kr.muroom.muroombackendbach.beta.registration.presetation.dto.RegistrationDto.CountResponse;
import kr.muroom.muroombackendbach.beta.registration.presetation.dto.RegistrationDto.GetResponse;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService.PresignedPutUrlDto;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedUrlsPutResponse;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedUrlsPutResponse.PresignedUrlInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RegistrationService {

  private final BetaRegistrationRepository betaRegistrationRepository;
  private final FileStorageService fileStorageService;

  public GeneratePresignedUrlsPutResponse generatePresignedPutUrls(
      RegistrationDto.GeneratePresignedUrlsRequest request) {
    List<PresignedUrlInfo> presignedUrlInfos = request.fileUploadRequests().stream()
        .map((fileRequest) -> {

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
        .agreedToPersonalInfoCollection(request.agreedToPersonalInfoCollection())
        .agreedToContentCollection(request.agreedToContentCollection())
        .agreedToThirdPartyProvision(request.agreedToThirdPartyProvision())
        .agreedToMarketing(request.agreedToMarketing() != null && request.agreedToMarketing())
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

  @Transactional(readOnly = true)
  public Page<GetResponse> getAllRegistrations(Pageable pageable) {
    Page<BetaRegistration> pagedRegistrations = betaRegistrationRepository.findAllWithImages(
        pageable);

    return pagedRegistrations.map(registration -> {
      List<String> introductoryImageUrls = registration.getIntroductoryImages().stream()
          .map(image ->
              fileStorageService.generatePresignedGetUrl(image.getFileKey()))
          .toList();

      return RegistrationDto.GetResponse.builder()
          .id(registration.getId())
          .name(registration.getName())
          .phoneNumber(registration.getPhoneNumber())
          .thirdPartyUrl(registration.getThirdPartyUrl())
          .agreedToPersonalInfoCollection(registration.getAgreedToPersonalInfoCollection())
          .agreedToContentCollection(registration.getAgreedToContentCollection())
          .agreedToThirdPartyProvision(registration.getAgreedToThirdPartyProvision())
          .agreedToMarketing(registration.getAgreedToMarketing())
          .featureSuggestions(registration.getFeatureSuggestions())
          .introductoryImageUrls(introductoryImageUrls)
          .createdAt(registration.getCreatedAt())
          .build();
    });
  }

  @Transactional(readOnly = true)
  public CountResponse getRegistrationCounts() {
    Long totalRegistrations = betaRegistrationRepository.countDistinctPhoneNumber();
    return CountResponse.builder()
        .totalRegistrations(totalRegistrations)
        .build();
  }
}
