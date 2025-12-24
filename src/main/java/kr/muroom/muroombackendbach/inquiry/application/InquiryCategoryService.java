package kr.muroom.muroombackendbach.inquiry.application;

import java.util.List;
import kr.muroom.muroombackendbach.inquiry.domain.repository.InquiryCategoryRepository;
import kr.muroom.muroombackendbach.inquiry.presentation.dto.response.InquiryCategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InquiryCategoryService {

  private final InquiryCategoryRepository inquiryCategoryRepository;

  public List<InquiryCategoryResponse> getAllInquiryCategories() {
    return inquiryCategoryRepository.findAll()
        .stream()
        .map(category -> InquiryCategoryResponse.builder()
            .id(String.valueOf(category.getId()))
            .code(category.getCode())
            .name(category.getName())
            .build())
        .toList();
  }
}
