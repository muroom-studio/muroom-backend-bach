package kr.muroom.muroombackendbach.inquiry.application;

import static kr.muroom.muroombackendbach.inquiry.exception.InquiryCategoryErrorCode.INQUIRY_CATEGORY_NOT_FOUND;
import static kr.muroom.muroombackendbach.inquiry.exception.InquiryErrorCode.INQUIRY_FORBIDDEN;
import static kr.muroom.muroombackendbach.inquiry.exception.InquiryErrorCode.INQUIRY_NOT_FOUND;
import static kr.muroom.muroombackendbach.user.exception.MusicianErrorCode.MUSICIAN_NOT_FOUND;

import java.util.List;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.inquiry.domain.entity.Inquiry;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryCategory;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryImage;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryReply;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryReplyImage;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryStatus;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryCategoryRepository;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryImageRepository;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryRepository;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.InquiryImageUploadRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.RegisterInquiryRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.SearchInquiryRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.ImageDto;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryAllResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryAllResponse.CategoryDto;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryResponse;
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

    return inquiries.map(inquiry -> SearchInquiryResponse.builder()
        .id(String.valueOf(inquiry.getId()))
        .title(inquiry.getTitle())
        .content(inquiry.getContent())
        .status(inquiry.getStatus())
        .category(SearchInquiryResponse.CategoryDto.from(inquiry.getCategory()))
        .images(toImageDtos(inquiry.getImages()))
        .createdAt(inquiry.getCreatedAt())
        .updatedAt(inquiry.getUpdatedAt())
        .build()
    );
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

    List<String> temporaryImageFileKeys = request.imageKeys();
    if (temporaryImageFileKeys == null || temporaryImageFileKeys.isEmpty()) {
      return;
    }

    List<String> permanentImageFileKeys = temporaryImageFileKeys.stream()
        .filter(k -> k != null && !k.isBlank())
        .map(String::trim)
        .distinct()
        .map(fileStorageService::movePrivateFileFromTempToPermanent)
        .toList();

    List<InquiryImage> inquiryImages = permanentImageFileKeys.stream()
        .map(permanentImageFileKey -> InquiryImage.builder()
            .inquiry(inquiry)
            .imageKey(permanentImageFileKey)
            .build())
        .toList();

    if (!permanentImageFileKeys.isEmpty()) {
      inquiryImageRepository.saveAll(inquiryImages);
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

  public GeneratePresignedPutUrlResponse generatePresignedPutUrl(InquiryImageUploadRequest request) {
    return fileStorageService.generatePresignedPutUrlForPrivate(request, FileStorageService::validateImageContentType);
  }

  private InquiryResponse toInquiryResponse(Inquiry inquiry) {

    return InquiryResponse.builder()
        .id(String.valueOf(inquiry.getId()))
        .title(inquiry.getTitle())
        .content(inquiry.getContent())
        .status(inquiry.getStatus())
        .category(toInquiryCategoryDto(inquiry))
        .reply(toInquiryReplyImageDto(inquiry))
        .images(toImageDtos(inquiry.getImages()))
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

    List<String> fileUrls = fileKeys.stream()
        .map(fileStorageService::generatePresignedGetUrlForPrivateFile)
        .toList();

    return InquiryResponse.Reply.builder()
        .id(String.valueOf(reply.getId()))
        .content(reply.getContent())
        .fileUrls(fileUrls)
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

  private InquiryAllResponse toResponse(Inquiry inquiry) {
    return InquiryAllResponse.builder()
        .id(String.valueOf(inquiry.getId()))
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

  private List<ImageDto> toImageDtos(List<InquiryImage> images) {
    return images.stream()
        .map(inquiryImage -> ImageDto.builder()
            .id(String.valueOf(inquiryImage.getId())) // InquiryImage의 id를 사용
            .imageFileUrl(fileStorageService.generatePresignedGetUrlForPrivateFile(inquiryImage.getImageKey())) // public URL 생성
            .build())
        .toList();
  }
}
