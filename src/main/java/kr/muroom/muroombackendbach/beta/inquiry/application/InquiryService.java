package kr.muroom.muroombackendbach.beta.inquiry.application;

import java.util.List;
import kr.muroom.muroombackendbach.beta.inquiry.domain.entity.BetaInquiry;
import kr.muroom.muroombackendbach.beta.inquiry.domain.repository.BetaInquiryRepository;
import kr.muroom.muroombackendbach.beta.inquiry.presentation.dto.InquiryDto;
import kr.muroom.muroombackendbach.beta.inquiry.presentation.dto.InquiryDto.GetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InquiryService {

  private final BetaInquiryRepository betaInquiryRepository;

  public void addNewInquiry(InquiryDto.CreateRequest request) {
    BetaInquiry newInquiry = BetaInquiry.builder()
        .name(request.name())
        .phoneNumber(request.phoneNumber())
        .content(request.content())
        .agreedToPrivacy(request.agreedToPrivacy())
        .build();

    betaInquiryRepository.save(newInquiry);
  }

  @Transactional(readOnly = true)
  public List<GetResponse> getAllInquiries() {
    List<BetaInquiry> inquiries = betaInquiryRepository.findAll();

    return inquiries.stream()
        .map(inquiry -> GetResponse.builder()
            .id(inquiry.getId())
            .name(inquiry.getName())
            .phoneNumber(inquiry.getPhoneNumber())
            .content(inquiry.getContent())
            .agreedToPrivacy(inquiry.getAgreedToPrivacy())
            .createdAt(inquiry.getCreatedAt())
            .build()
        )
        .toList();
  }
}
