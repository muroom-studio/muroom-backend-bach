package kr.muroom.muroombackendbach.terms.application;

import static kr.muroom.muroombackendbach.terms.presentation.dto.TermDto.*;

import java.util.ArrayList;
import java.util.Collections;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.util.VersionUtil;
import kr.muroom.muroombackendbach.terms.domain.entity.TargetRole;
import kr.muroom.muroombackendbach.terms.domain.entity.Term;
import kr.muroom.muroombackendbach.terms.domain.entity.TermContent;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import kr.muroom.muroombackendbach.terms.domain.repository.TermContentRepository;
import kr.muroom.muroombackendbach.terms.domain.repository.TermRepository;
import kr.muroom.muroombackendbach.terms.exception.TermErrorCode;
import kr.muroom.muroombackendbach.terms.presentation.dto.TermAllByCodeResponse;
import kr.muroom.muroombackendbach.terms.presentation.dto.TermRegisterRequest;
import kr.muroom.muroombackendbach.terms.presentation.dto.TermUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TermService {

  private final TermRepository termRepository;
  private final TermContentRepository termContentRepository;

  public List<TermsWithContentDto> getTermsMusicianByType() {
    return termRepository.findLatestTermsByRoleAndTypes(TargetRole.MUSICIAN);
  }

  public List<TermsWithContentDto> getTermsOwnerByType(List<TermsType> types) {
    return termRepository.findLatestTermsByRoleAndTypes(TargetRole.OWNER);
  }

  public TermContentDto getTermContent(Long termId) {
    TermContent termContent = termContentRepository.findById(termId)
        .orElseThrow(() -> new BusinessException(
            TermErrorCode.NOT_EXIST_TERM));

    return new TermContentDto(termId, termContent.getContent());
  }

  @Transactional
  public void registerMusicianTerms(TermRegisterRequest request) {
    List<TermsWithContentDto> latestTerm = termRepository.findLatestTermsByRoleAndTypes(
        request.targetRole());

    TermsWithContentDto latestForCode = latestTerm.stream()
        .filter(t -> t.code() == request.code()) // code가 enum이면 '==' 가능, 아니면 equals() 사용
        .findFirst()
        .orElse(null);

    String nextVersion = (latestForCode == null)
        ? "0.0.1"
        : VersionUtil.nextVersion(latestForCode.version());

    Term term = Term.builder()
        .effectiveAt(request.effectiveAt())
        .targetRole(request.targetRole())
        .isMandatory(request.isMandatory())
        .version(nextVersion)
        .code(request.code()).build();
    Term save = termRepository.save(term);

    TermContent termContent = TermContent.builder()
        .term(save)
        .content(request.content()).build();
    termContentRepository.save(termContent);
  }

  @Transactional
  public void updateMusicianTerms(Long termId, TermUpdateRequest request) {
    TermContent termContent = termContentRepository.findById(termId)
        .orElseThrow(() -> new BusinessException(
            TermErrorCode.NOT_EXIST_TERM));

    termContent.updateContent(request.content());

    Term term = termContent.getTerm();
    term.updateTerm(request.code()
        , request.targetRole()
        , request.effectiveAt()
    );
  }

  public List<TermAllByCodeResponse> getAllTermByCode(TermsType code, TargetRole targetRole) {
    List<Term> terms = termRepository.findByCodeAndTargetRoleOrderByVersionAsc(code,
        targetRole);

    return terms.stream()
        .map(TermAllByCodeResponse::from)
        .toList();
  }
}
