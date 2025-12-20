package kr.muroom.muroombackendbach.faq.application;

import kr.muroom.muroombackendbach.faq.domain.repository.FaqRepository;
import kr.muroom.muroombackendbach.faq.presentation.dto.response.FaqResponse;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FaqService {

  private final FaqRepository faqRepository;

  @Transactional(readOnly = true)
  public Page<FaqResponse> findFaqs(String keyword, Long categoryId, Pageable pageable) {

    boolean hasKeyword = StringUtils.hasText(keyword);
    boolean hasCategory = categoryId != null;

    if (!hasKeyword && !hasCategory) {
      return faqRepository.findAllByDeletedAtIsNullAndCategory_IsActiveTrue(pageable)
          .map(FaqResponse::from);
    }

    if (hasKeyword && !hasCategory) {
      return faqRepository.searchForClient(keyword.trim(), pageable)
          .map(FaqResponse::from);
    }

    if (!hasKeyword) {
      return faqRepository.findAllByCategory(
              categoryId, pageable
          )
          .map(FaqResponse::from);
    }

    return faqRepository.searchByKeywordAndCategory(
            keyword.trim(), categoryId, pageable
        )
        .map(FaqResponse::from);
  }
}
