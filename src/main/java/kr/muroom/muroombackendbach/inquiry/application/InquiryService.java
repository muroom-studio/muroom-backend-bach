package kr.muroom.muroombackendbach.inquiry.application;

import static kr.muroom.muroombackendbach.inquiry.exception.InquiryCategoryErrorCode.INQUIRY_CATEGORY_NOT_FOUND;
import static kr.muroom.muroombackendbach.inquiry.exception.InquiryErrorCode.INQUIRY_FORBIDDEN;
import static kr.muroom.muroombackendbach.inquiry.exception.InquiryErrorCode.INQUIRY_NOT_FOUND;
import static kr.muroom.muroombackendbach.user.exception.MusicianErrorCode.MUSICIAN_NOT_FOUND;

import java.util.List;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService.PresignedPutUrlDto;
import kr.muroom.muroombackendbach.filestorage.exception.FileErrorCode;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedUrlsPutResponse;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedUrlsPutResponse.PresignedUrlInfo;
import kr.muroom.muroombackendbach.inquiry.domain.entity.Inquiry;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryCategory;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryImage;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryReply;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryReplyImage;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryStatus;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryCategoryRepository;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryImageRepository;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryRepository;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.InquiryImagePresignedUrlRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.SearchInquiryRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryAllResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryAllResponse.CategoryDto;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryAllResponse.ImageDto;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.RegisterInquiryRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.SearchInquiryResponse;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.user.domain.repository.MusicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

  private final MusicianRepository musicianRepository;
  private final InquiryRepository inquiryRepository;
  private final InquiryCategoryRepository inquiryCategoryRepository;
  private final InquiryImageRepository inquiryImageRepository;
  private final FileStorageService fileStorageService;

  public Page<SearchInquiryResponse> searchInquiry(Long musicianId,
      SearchInquiryRequest req, Pageable pageable) {

    String keyword = req.keyword();
    if (keyword == null || keyword.isBlank()) {
      keyword = "";
    }

    Page<Inquiry> inquiries = inquiryRepository.searchByKeyword(musicianId, keyword, pageable);
    return inquiries.map(SearchInquiryResponse::from);
  }

  @Transactional
  public void registerInquiry(Long musicianId, RegisterInquiryRequest request) {
    Musician musician = musicianRepository.findById(musicianId)
        .orElseThrow(() -> new BusinessException(MUSICIAN_NOT_FOUND));

    InquiryCategory inquiryCategory = inquiryCategoryRepository.findById(request.categoryId())
        .orElseThrow(() -> new BusinessException(INQUIRY_CATEGORY_NOT_FOUND));

    Inquiry inquiry = Inquiry.builder()
        .musician(musician)
        .category(inquiryCategory)
        .title(request.title())
        .content(request.content())
        .status(InquiryStatus.PROCESSING)
        .build();

    inquiryRepository.save(inquiry);

    List<String> keys = request.imageKeys();
    if (keys == null || keys.isEmpty()) {
      return;
    }

    List<InquiryImage> images = keys.stream()
        .filter(k -> k != null && !k.isBlank())
        .map(String::trim)
        .distinct()
        .map(key -> InquiryImage.builder()
            .inquiry(inquiry)
            .imageKey(key)
            .build())
        .toList();

    if (!images.isEmpty()) {
      inquiryImageRepository.saveAll(images);
    }
  }

  public Page<InquiryAllResponse> getAllMyInquiry(Long musicianId, Pageable pageable) {
    if (!musicianRepository.existsById(musicianId)) {
      throw new BusinessException(MUSICIAN_NOT_FOUND);
    }

    return inquiryRepository.findAllByMusicianId(musicianId, pageable)
        .map(this::toResponse);
  }

  public InquiryResponse getInquiry(Long musicianId, Long inquiryId) {
    if (!musicianRepository.existsById(musicianId)) {
      throw new BusinessException(MUSICIAN_NOT_FOUND);
    }

    Inquiry inquiry = inquiryRepository.findById(inquiryId)
        .orElseThrow(() -> new BusinessException(INQUIRY_NOT_FOUND));

    if (!inquiry.getMusician().getId().equals(musicianId)) {
      throw new BusinessException(INQUIRY_FORBIDDEN);
    }

    return toInquiryResponse(inquiry);
  }

  public GeneratePresignedUrlsPutResponse generatePresignedPutUrls(
      InquiryImagePresignedUrlRequest request) {
    List<PresignedUrlInfo> presignedUrlInfos = request.inquiryImages().stream()
        .map((inquiryImageInfo) -> {
          validateContentType(inquiryImageInfo.contentType());

          InquiryCategory inquiryCategory = inquiryCategoryRepository.findById(
                  inquiryImageInfo.categoryId())
              .orElseThrow(() -> new BusinessException(INQUIRY_CATEGORY_NOT_FOUND));

          String domain = "inquiries/" + inquiryCategory.getCode().toLowerCase();
          PresignedPutUrlDto singleUrlDto = fileStorageService.generatePresignedPutUrl(
              inquiryImageInfo.fileName(), domain, inquiryImageInfo.contentType()
          );

          return new PresignedUrlInfo(singleUrlDto.url(), singleUrlDto.fileKey());
        })
        .toList();

    return new GeneratePresignedUrlsPutResponse(presignedUrlInfos);
  }

  private void validateContentType(String contentType) {
    if (!contentType.startsWith("image/")) {
      throw new BusinessException(FileErrorCode.UNSUPPORTED_FILE_TYPE);
    }
  }

  private InquiryResponse toInquiryResponse(Inquiry inquiry) {
    return InquiryResponse.builder()
        .id(inquiry.getId())
        .title(inquiry.getTitle())
        .content(inquiry.getContent())
        .status(inquiry.getStatus())
        .category(toInquiryCategoryDto(inquiry))
        .reply(toInquiryReplyImageDto(inquiry))
        .images(toInquiryImageDtos(inquiry.getImages()))
        .createdAt(inquiry.getCreatedAt())
        .updatedAt(inquiry.getUpdatedAt())
        .build();
  }

  private InquiryResponse.Reply toInquiryReplyImageDto(Inquiry inquiry) {
    InquiryReply reply = inquiry.getInquiryReply();
    if (reply == null) {
      return null;
    }

    List<String> fileKeys = reply.getInquiryReplyImages() == null
        ? List.of()
        : reply.getInquiryReplyImages().stream()
            .map(InquiryReplyImage::getImageKey)
            .filter(k -> k != null && !k.isBlank())
            .map(String::trim)
            .distinct()
            .toList();

    return InquiryResponse.Reply.builder()
        .id(reply.getId())
        .content(reply.getContent())
        .fileKeys(fileKeys)
        .build();
  }

  private InquiryResponse.CategoryDto toInquiryCategoryDto(Inquiry inquiry) {
    if (inquiry.getCategory() == null) {
      return null;
    }
    return InquiryResponse.CategoryDto.builder()
        .code(inquiry.getCategory().getCode())
        .name(inquiry.getCategory().getName())
        .build();
  }

  private List<InquiryResponse.ImageDto> toInquiryImageDtos(List<InquiryImage> images) {
    if (images == null) {
      return List.of();
    }
    return images.stream()
        .map(img -> InquiryResponse.ImageDto.builder()
            .id(img.getId())
            .imageKey(img.getImageKey())
            .build())
        .toList();
  }

  private InquiryAllResponse toResponse(Inquiry inquiry) {
    return InquiryAllResponse.builder()
        .id(inquiry.getId())
        .title(inquiry.getTitle())
        .content(inquiry.getContent())
        .status(inquiry.getStatus())
        .category(toCategoryDto(inquiry))
        .images(toImageDtos(inquiry.getImages()))
        .createdAt(inquiry.getCreatedAt())
        .updatedAt(inquiry.getUpdatedAt())
        .build();
  }

  private InquiryAllResponse.CategoryDto toCategoryDto(Inquiry inquiry) {
    if (inquiry.getCategory() == null) {
      return null;
    }
    return CategoryDto.builder()
        .code(inquiry.getCategory().getCode())
        .name(inquiry.getCategory().getName())
        .build();
  }

  private List<InquiryAllResponse.ImageDto> toImageDtos(List<InquiryImage> images) {
    if (images == null) {
      return List.of();
    }
    return images.stream()
        .map(img -> ImageDto.builder()
            .id(img.getId())
            .imageKey(img.getImageKey())
            .build())
        .toList();
  }
}
