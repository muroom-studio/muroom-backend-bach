package kr.muroom.muroombackendbach.inquiry.application;

import static kr.muroom.muroombackendbach.inquiry.exception.InquiryErrorCode.INQUIRY_NOT_FOUND;
import static kr.muroom.muroombackendbach.inquiry.exception.InquiryReplyErrorCode.INQUIRY_REPLY_NOT_FOUND;

import java.util.List;
import kr.muroom.muroombackendbach.admin.inquiry.presentation.dto.InquiryReplyImageUploadRequest;
import kr.muroom.muroombackendbach.admin.inquiry.presentation.dto.InquiryReplyRequest;
import kr.muroom.muroombackendbach.admin.inquiry.presentation.dto.UpdateInquiryReplyRequest;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.exception.FileErrorCode;
import kr.muroom.muroombackendbach.filestorage.presentation.dto.response.GeneratePresignedPutUrlResponse;
import kr.muroom.muroombackendbach.inquiry.domain.entity.Inquiry;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryReply;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryReplyImage;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryReplyImageRepository;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryReplyRepository;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryReplyService {

  private final InquiryRepository inquiryRepository;
  private final InquiryReplyRepository inquiryReplyRepository;
  private final FileStorageService fileStorageService;
  private final InquiryReplyImageRepository inquiryReplyImageRepository;

  @Transactional(readOnly = true)
  public GeneratePresignedPutUrlResponse generatePresignedPutUrl(InquiryReplyImageUploadRequest request) {
    return fileStorageService.generatePresignedPutUrlForPrivate(request, FileStorageService::validateImageContentType);
  }

  private void validateContentType(String contentType) {
    if (!contentType.startsWith("image/")) {
      throw new BusinessException(FileErrorCode.UNSUPPORTED_FILE_TYPE);
    }
  }

  public void registerInquiryReply(Long inquiryId, InquiryReplyRequest request) {
    Inquiry inquiry = inquiryRepository.findById(inquiryId)
        .orElseThrow(() -> new BusinessException(INQUIRY_NOT_FOUND));

    InquiryReply reply = InquiryReply.builder()
        .inquiry(inquiry)
        .content(request.content())
        .build();

    inquiryReplyRepository.save(reply);

    List<String> keys = request.imageKeys();
    if (keys == null || keys.isEmpty()) {
      return;
    }

    // 빈 값 제거 + trim + 중복 제거(순서 유지)
    List<InquiryReplyImage> images = keys.stream()
        .filter(k -> k != null && !k.isBlank())
        .map(String::trim)
        .distinct()
        .map(key -> InquiryReplyImage.builder()
            .inquiryReply(reply)
            .imageKey(key)
            .build())
        .toList();

    if (!images.isEmpty()) {
      inquiryReplyImageRepository.saveAll(images);
    }
  }

  public void updateInquiryReply(Long inquiryReplyId, UpdateInquiryReplyRequest request) {
    InquiryReply inquiryReply = inquiryReplyRepository.findById(inquiryReplyId)
        .orElseThrow(() -> new BusinessException(INQUIRY_REPLY_NOT_FOUND));

    inquiryReply.changeContent(request.content().trim());

    List<String> imageKeys = request.inquiryReplyImages();
    inquiryReply.replaceImages(imageKeys);
  }

  public void deleteInquiryReply(Long inquiryReplyId) {
    InquiryReply inquiryReply = inquiryReplyRepository.findById(inquiryReplyId)
        .orElseThrow(() -> new BusinessException(INQUIRY_REPLY_NOT_FOUND));

    inquiryReplyRepository.delete(inquiryReply);
  }
}
