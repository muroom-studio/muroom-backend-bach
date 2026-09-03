package kr.muroom.muroombackendbach.inquiry.presentation.dto;

import java.util.List;
import kr.muroom.muroombackendbach.filestorage.application.FileStorageService;
import kr.muroom.muroombackendbach.filestorage.domain.FileStorageLocation;
import kr.muroom.muroombackendbach.inquiry.domain.entity.Inquiry;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryCategory;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryImage;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryReply;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryReplyImage;
import kr.muroom.muroombackendbach.inquiry.domain.entity.InquiryStatus;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.request.RegisterInquiryRequest;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.ImageDto;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryAllResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryResponse;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.SearchInquiryResponse;
import kr.muroom.muroombackendbach.musician.domain.entity.Musician;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InquiryAssembler {

  private final FileStorageService fileStorageService;

  /**
   * 내부 공통 모델: reply 변환 시 presigned URL 생성/필터링 같은 핵심 로직은 여기서만.
   */
  private record ReplyView(String id, String content, List<ImageDto> imageUrls) {

  }

  public Inquiry toEntity(Musician musician, InquiryCategory category,
      RegisterInquiryRequest request) {
    return Inquiry.builder()
        .musician(musician)
        .category(category)
        .title(request.title())
        .content(request.content())
        .status(InquiryStatus.PROCESSING)
        .build();
  }

  public SearchInquiryResponse toSearchInquiryResponse(Inquiry inquiry) {
    return SearchInquiryResponse.builder()
        .id(String.valueOf(inquiry.getId()))
        .title(inquiry.getTitle())
        .content(inquiry.getContent())
        .status(inquiry.getStatus())
        .category(SearchInquiryResponse.CategoryDto.from(inquiry.getCategory()))
        .images(toInquiryImageDtos(inquiry.getImages()))
        .reply(toSearchReply(inquiry))
        .createdAt(inquiry.getCreatedAt())
        .updatedAt(inquiry.getUpdatedAt())
        .build();
  }

  public InquiryAllResponse toInquiryAllResponse(Inquiry inquiry) {
    return InquiryAllResponse.builder()
        .id(String.valueOf(inquiry.getId()))
        .title(inquiry.getTitle())
        .content(inquiry.getContent())
        .status(inquiry.getStatus())
        .category(InquiryAllResponse.CategoryDto.from(inquiry.getCategory()))
        .images(toInquiryImageDtos(inquiry.getImages()))
        .reply(toAllReply(inquiry))
        .createdAt(inquiry.getCreatedAt())
        .updatedAt(inquiry.getUpdatedAt())
        .build();
  }

  public InquiryResponse toInquiryResponse(Inquiry inquiry) {
    return InquiryResponse.builder()
        .id(String.valueOf(inquiry.getId()))
        .title(inquiry.getTitle())
        .content(inquiry.getContent())
        .status(inquiry.getStatus())
        .category(InquiryResponse.CategoryDto.from(inquiry.getCategory()))
        .reply(toInquiryReply(inquiry))
        .images(toInquiryImageDtos(inquiry.getImages()))
        .createdAt(inquiry.getCreatedAt())
        .updatedAt(inquiry.getUpdatedAt())
        .build();
  }

  private SearchInquiryResponse.Reply toSearchReply(Inquiry inquiry) {
    ReplyView v = toReplyView(inquiry);
    if (v == null) {
      return null;
    }

    return SearchInquiryResponse.Reply.builder()
        .id(v.id())
        .content(v.content())
        .imageUrls(v.imageUrls())
        .build();
  }

  private InquiryAllResponse.Reply toAllReply(Inquiry inquiry) {
    ReplyView v = toReplyView(inquiry);
    if (v == null) {
      return null;
    }

    return InquiryAllResponse.Reply.builder()
        .id(v.id())
        .content(v.content())
        .imageUrls(v.imageUrls())
        .build();
  }

  private InquiryResponse.Reply toInquiryReply(Inquiry inquiry) {
    ReplyView v = toReplyView(inquiry);
    if (v == null) {
      return null;
    }

    return InquiryResponse.Reply.builder()
        .id(v.id())
        .content(v.content())
        .imageUrls(v.imageUrls())
        .build();
  }

  private ReplyView toReplyView(Inquiry inquiry) {
    InquiryReply reply = inquiry.getInquiryReply();
    if (reply == null) {
      return null;
    }

    List<ImageDto> imageDtos =
        reply.getInquiryReplyImages() == null
            ? List.of()
            : reply.getInquiryReplyImages().stream()
                .filter(img -> img.getImageKey() != null && !img.getImageKey().isBlank())
                .map(this::toReplyImageDto)
                .toList();

    return new ReplyView(
        String.valueOf(reply.getId()),
        reply.getContent(),
        imageDtos
    );
  }

  private ImageDto toReplyImageDto(InquiryReplyImage img) {
    return ImageDto.builder()
        .id(String.valueOf(img.getId()))
        .imageFileUrl(fileStorageService.getViewUrl(img.getImageKey(), FileStorageLocation.PRIVATE_PERMANENT))
        .build();
  }

  // =========================
  // Inquiry image mapping
  // =========================

  private List<ImageDto> toInquiryImageDtos(List<InquiryImage> images) {
    if (images == null) {
      return List.of();
    }

    return images.stream()
        .filter(this::hasValidImageKey)
        .map(img -> ImageDto.builder()
            .id(String.valueOf(img.getId()))
            .imageFileUrl(
                fileStorageService.getViewUrl(img.getImageKey(), FileStorageLocation.PRIVATE_PERMANENT))
            .build())
        .toList();
  }

  private boolean hasValidImageKey(InquiryImage img) {
    return img != null && img.getImageKey() != null && !img.getImageKey().isBlank();
  }
}
