package kr.muroom.muroombackendbach.beta.inquiry.application;

import kr.muroom.muroombackendbach.beta.inquiry.domain.entity.BetaInquiry;
import kr.muroom.muroombackendbach.beta.inquiry.domain.repository.BetaInquiryRepository;
import kr.muroom.muroombackendbach.beta.inquiry.presentation.dto.InquiryDto;
import kr.muroom.muroombackendbach.beta.inquiry.presentation.dto.InquiryDto.GetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 문의사항 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class InquiryService {

  private final BetaInquiryRepository betaInquiryRepository;

  /**
   * 새로운 문의사항을 추가합니다.
   *
   * @param request 문의사항 생성 요청 데이터
   */
  public void addNewInquiry(InquiryDto.CreateRequest request) {
    BetaInquiry newInquiry = BetaInquiry.builder()
        .name(request.name())
        .phoneNumber(request.phoneNumber())
        .content(request.content())
        .agreedToPrivacy(request.agreedToPrivacy())
        .build();

    betaInquiryRepository.save(newInquiry);
  }

  /**
   * 모든 문의사항을 페이지 형태로 조회합니다.
   *
   * @param pageable 페이지네이션 정보
   * @return 문의사항 페이지
   */
  @Transactional(readOnly = true)
  public Page<GetResponse> getAllInquiries(Pageable pageable) {
    Page<BetaInquiry> pagedInquiries = betaInquiryRepository.findAll(pageable);

    return pagedInquiries.map(inquiry -> GetResponse.builder()
        .id(inquiry.getId())
        .name(inquiry.getName())
        .phoneNumber(inquiry.getPhoneNumber())
        .content(inquiry.getContent())
        .agreedToPrivacy(inquiry.getAgreedToPrivacy())
        .createdAt(inquiry.getCreatedAt())
        .build());
  }
}
