package kr.muroom.muroombackendbach.terms.application;

import java.util.Comparator;
import java.util.List;
import kr.muroom.muroombackendbach.common.exception.BusinessException;
import kr.muroom.muroombackendbach.common.util.VersionUtil;
import kr.muroom.muroombackendbach.terms.domain.entity.TargetRole;
import kr.muroom.muroombackendbach.terms.domain.entity.Term;
import kr.muroom.muroombackendbach.terms.domain.entity.TermContent;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import kr.muroom.muroombackendbach.terms.domain.repository.TermContentRepository;
import kr.muroom.muroombackendbach.terms.domain.repository.TermRepository;
import kr.muroom.muroombackendbach.terms.exception.TermErrorCode;
import kr.muroom.muroombackendbach.terms.presentation.dto.TermAssembler;
import kr.muroom.muroombackendbach.terms.presentation.dto.request.TermRegisterRequest;
import kr.muroom.muroombackendbach.terms.presentation.dto.request.TermUpdateRequest;
import kr.muroom.muroombackendbach.terms.presentation.dto.response.TermDetailResponse;
import kr.muroom.muroombackendbach.terms.presentation.dto.response.TermSimpleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TermService {

  private final TermRepository termRepository;
  private final TermContentRepository termContentRepository;
  private final TermAssembler termAssembler;

  public List<TermDetailResponse> getTermsMusicianByType() {
    List<TermDetailResponse> terms = termRepository.findLatestTermsByRoleAndTypes(
        TargetRole.MUSICIAN);
    List<TermsType> desiredOrder = List.of(TermsType.TERMS_OF_USE, TermsType.PRIVACY_COLLECTION,
        TermsType.MARKETING_RECEIVE);
    terms.sort(Comparator.comparingInt(term -> desiredOrder.indexOf(term.code())));
    return terms;
  }

  public List<TermDetailResponse> getTermsOwnerByType(List<TermsType> types) {
    return termRepository.findLatestTermsByRoleAndTypes(TargetRole.OWNER);
  }

  public TermSimpleResponse getTermContent(Long termId) {
    TermContent termContent = termContentRepository.findById(termId)
        .orElseThrow(() -> new BusinessException(
            TermErrorCode.NOT_EXIST_TERM));

    return termAssembler.toTermSimpleResponse(termId, termContent);
  }

  @Transactional
  public void registerMusicianTerms(TermRegisterRequest request) {
    List<TermDetailResponse> latestTerm =
        termRepository.findLatestTermsByRoleAndTypes(request.targetRole());

    TermDetailResponse latestForCode = latestTerm.stream()
        .filter(t -> t.code() == request.code())
        .findFirst()
        .orElse(null);

    String nextVersion = (latestForCode == null)
        ? "0.0.1"
        : VersionUtil.nextVersion(latestForCode.version());

    Term term = termAssembler.createTermFromRegisterRequest(
        request,
        nextVersion
    );
    Term savedTerm = termRepository.save(term);

    TermContent termContent =
        termAssembler.createTermContent(savedTerm, request);

    termContentRepository.save(termContent);
  }

  @Transactional
  public void updateMusicianTerms(Long termId, TermUpdateRequest request) {
    TermContent termContent = termContentRepository.findById(termId)
        .orElseThrow(() -> new BusinessException(
            TermErrorCode.NOT_EXIST_TERM));

    termContent.updateContent(request.content());

    Term term = termContent.getTerm();
    term.updateTerm(request.code(), request.targetRole(), request.effectiveAt());
  }
}
