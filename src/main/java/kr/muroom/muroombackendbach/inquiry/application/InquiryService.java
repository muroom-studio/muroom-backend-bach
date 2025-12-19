package kr.muroom.muroombackendbach.inquiry.application;

import static kr.muroom.muroombackendbach.inquiry.exception.InquiryCategoryErrorCode.INQUIRY_CATEGORY_NOT_FOUND;
import static kr.muroom.muroombackendbach.inquiry.exception.InquiryErrorCode.INQUIRY_FORBIDDEN;
import static kr.muroom.muroombackendbach.inquiry.exception.InquiryErrorCode.INQUIRY_NOT_FOUND;
import static kr.muroom.muroombackendbach.user.exception.MusicianErrorCode.MUSICIAN_NOT_FOUND;

import java.util.List;
import kr.muroom.muroombackendbach.admin.inquiry.presentation.dto.InquiryReplyRequest;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.inquiry.domain.entity.Inquiry;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryCategory;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryImage;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryReply;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryStatus;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryCategoryRepository;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryReplyRepository;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryRepository;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.InquiryAllResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.InquiryAllResponse.CategoryDto;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.InquiryAllResponse.ImageDto;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.InquiryResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.RegisterInquiryRequest;
import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import kr.muroom.muroombackendbach.user.domain.repository.MusicianRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

  private final MusicianRepository musicianRepository;
  private final InquiryRepository inquiryRepository;
  private final InquiryCategoryRepository inquiryCategoryRepository;
  private final InquiryReplyRepository inquiryReplyRepository;
  private final FileStorageService fileStorageService;

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
  }

  @Transactional
  public void registerInquiryReply(Long inquiryId, InquiryReplyRequest request) {
    Inquiry inquiry = inquiryRepository.findById(inquiryId)
        .orElseThrow(() -> new BusinessException(INQUIRY_NOT_FOUND));

    InquiryReply reply = InquiryReply.builder()
        .inquiry(inquiry)
        .content(request.content())
        .build();

    inquiryReplyRepository.save(reply);
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

  private InquiryResponse toInquiryResponse(Inquiry inquiry) {
    return InquiryResponse.builder()
        .id(inquiry.getId())
        .title(inquiry.getTitle())
        .content(inquiry.getContent())
        .status(inquiry.getStatus())
        .category(toInquiryCategoryDto(inquiry))
        .images(toInquiryImageDtos(inquiry.getImages()))
        .createdAt(inquiry.getCreatedAt())
        .updatedAt(inquiry.getUpdatedAt())
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

  public List<InquiryAllResponse> getMyInquiry(Long musicianId) {
    if (!musicianRepository.existsById(musicianId)) {
      throw new BusinessException(MUSICIAN_NOT_FOUND);
    }

    return inquiryRepository.findAllByMusicianIdOrderByCreatedAtDesc(musicianId)
        .stream()
        .map(this::toResponse)
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
