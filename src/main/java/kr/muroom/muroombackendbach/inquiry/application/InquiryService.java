package kr.muroom.muroombackendbach.inquiry.application;

import static kr.muroom.muroombackendbach.inquiry.exception.InquiryCategoryErrorCode.INQUIRY_CATEGORY_NOT_FOUND;
import static kr.muroom.muroombackendbach.inquiry.exception.InquiryErrorCode.INQUIRY_FORBIDDEN;
import static kr.muroom.muroombackendbach.inquiry.exception.InquiryErrorCode.INQUIRY_NOT_FOUND;
import static kr.muroom.muroombackendbach.user.exception.MusicianErrorCode.MUSICIAN_NOT_FOUND;

import java.util.List;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.inquiry.application.mapper.InquiryResponseMapper;
import kr.muroom.muroombackendbach.inquiry.domain.entity.Inquiry;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryCategory;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryImage;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryStatus;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryCategoryRepository;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryImageRepository;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryRepository;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.InquiryImageUploadRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.RegisterInquiryRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryAllResponse;
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
  private final InquiryResponseMapper inquiryResponseMapper;

  public Page<SearchInquiryResponse> searchInquiry(Long musicianId, String keyword,
      Pageable pageable) {
    if (keyword == null || keyword.isBlank()) {
      keyword = "";
    }

    return inquiryRepository.searchByKeyword(musicianId, keyword, pageable)
        .map(inquiryResponseMapper::toSearchInquiryResponse);
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

    if (permanentImageFileKeys.isEmpty()) {
      return;
    }

    List<InquiryImage> inquiryImages = permanentImageFileKeys.stream()
        .map(permanentKey -> InquiryImage.builder()
            .inquiry(inquiry)
            .imageKey(permanentKey)
            .build())
        .toList();

    inquiryImageRepository.saveAll(inquiryImages);
  }

  public Page<InquiryAllResponse> getAllMyInquiry(Long musicianId, Pageable pageable) {
    if (!musicianRepository.existsById(musicianId)) {
      throw new BusinessException(MUSICIAN_NOT_FOUND);
    }

    return inquiryRepository.findAllByMusicianId(musicianId, pageable)
        .map(inquiryResponseMapper::toInquiryAllResponse);
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

    return inquiryResponseMapper.toInquiryResponse(inquiry);
  }

  public GeneratePresignedPutUrlResponse generatePresignedPutUrl(
      InquiryImageUploadRequest request) {
    return fileStorageService.generatePresignedPutUrlForPrivate(
        request, FileStorageService::validateImageContentType);
  }
}
