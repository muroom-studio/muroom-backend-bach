package kr.muroom.muroombackendbach.faq.application;

import java.util.List;
import kr.muroom.muroombackendbach.faq.domain.entity.FaqCategory;
import kr.muroom.muroombackendbach.faq.domain.repository.FaqCategoryRepository;
import kr.muroom.muroombackendbach.faq.presentation.dto.response.FaqCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FaqCategoryService {

  private final FaqCategoryRepository faqCategoryRepository;

  @Transactional(readOnly = true)
  public List<FaqCategoryResponse> getAllFaqCategory() {
    return faqCategoryRepository.findAllByIsActiveTrue(
            Sort.by(
                Sort.Order.asc("displayOrder"),
                Sort.Order.asc("id")
            )
        )
        .stream()
        .map(FaqCategoryResponse::from)
        .toList();
  }
}
