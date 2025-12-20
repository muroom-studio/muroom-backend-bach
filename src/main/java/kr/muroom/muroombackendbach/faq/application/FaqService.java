package kr.muroom.muroombackendbach.faq.application;

import kr.muroom.muroombackendbach.faq.domain.repository.FaqRepository;
import kr.muroom.muroombackendbach.faq.presentation.dto.response.FaqResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FaqService {

  private final FaqRepository faqRepository;

  @Transactional(readOnly = true)
  public Page<FaqResponse> getAllFaqs(Pageable pageable) {
    return faqRepository.findAll(pageable).map(FaqResponse::from);
  }
}
