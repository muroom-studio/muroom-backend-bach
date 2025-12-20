package kr.muroom.muroombackendbach.faq.application;

import static kr.muroom.muroombackendbach.faq.exception.FaqErrorCode.FAQ_NOT_FOUND;

import kr.muroom.muroombackendbach.admin.faq.presentation.dto.RegisterFaqRequest;
import kr.muroom.muroombackendbach.admin.faq.presentation.dto.UpdateFaqRequest;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.faq.domain.entity.Faq;
import kr.muroom.muroombackendbach.faq.domain.entity.FaqCategory;
import kr.muroom.muroombackendbach.faq.domain.repository.FaqCategoryRepository;
import kr.muroom.muroombackendbach.faq.domain.repository.FaqRepository;
import kr.muroom.muroombackendbach.faq.exception.FaqErrorCode;
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
  private final FaqCategoryRepository faqCategoryRepository;

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

  @Transactional
  public void registerFaq(RegisterFaqRequest request) {
    FaqCategory faqCategory = faqCategoryRepository.findById(request.categoryId())
        .orElseThrow(() -> new BusinessException(
            FaqErrorCode.FAQ_CATEGORY_NOT_FOUND));

    Faq faq = Faq.builder()
        .question(request.question())
        .answer(request.answer())
        .category(faqCategory)
        .build();

    faqRepository.save(faq);
  }

  @Transactional
  public void updateFaq(Long faqId, UpdateFaqRequest request) {
    Faq faq = faqRepository.findById(faqId).orElseThrow(() -> new BusinessException(FAQ_NOT_FOUND));
    faq.updateFaq(request.question(), request.answer());
  }

  @Transactional
  public void deleteFaq(Long faqId) {
    Faq faq = faqRepository.findById(faqId).orElseThrow(() -> new BusinessException(FAQ_NOT_FOUND));
    faqRepository.delete(faq);
  }
}
