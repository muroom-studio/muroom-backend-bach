package kr.muroom.muroombackendbach.terms.application;

import static kr.muroom.muroombackendbach.terms.presentation.dto.TermDto.*;

import kr.muroom.muroombackendbach.terms.domain.entity.TermContent;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import kr.muroom.muroombackendbach.terms.domain.repository.TermContentRepository;
import kr.muroom.muroombackendbach.terms.domain.repository.TermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TermService {

  private final TermRepository termRepository;
  private final TermContentRepository termContentRepository;

  public List<TermsWithContentDto> getTermsMusicianByType(List<TermsType> types) {
    return termRepository.findLatestTermsByRoleAndTypes("MUSICIAN", types);
  }

  public List<TermsWithContentDto> getTermsOwnerByType(List<TermsType> types) {
    return termRepository.findLatestTermsByRoleAndTypes("OWNER", types);
  }

  public TermContentDto getTermContent(Long termId) {
    TermContent termContent = termContentRepository.findById(termId).orElseThrow();
    return new TermContentDto(termContent.getTerm().getId(), termContent.getContent());
  }
}
