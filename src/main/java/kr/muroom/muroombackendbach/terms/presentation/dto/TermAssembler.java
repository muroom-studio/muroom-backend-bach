package kr.muroom.muroombackendbach.terms.presentation.dto;

import kr.muroom.muroombackendbach.terms.domain.entity.Term;
import kr.muroom.muroombackendbach.terms.domain.entity.TermContent;
import kr.muroom.muroombackendbach.terms.presentation.dto.request.TermRegisterRequest;
import kr.muroom.muroombackendbach.terms.presentation.dto.response.TermSimpleResponse;
import org.springframework.stereotype.Component;

@Component
public class TermAssembler {

  /**
   * 신규 약관 등록용 Term 생성
   */
  public Term createTermFromRegisterRequest(
      TermRegisterRequest request,
      String version
  ) {
    return Term.builder()
        .effectiveAt(request.effectiveAt())
        .targetRole(request.targetRole())
        .isMandatory(request.isMandatory())
        .version(version)
        .code(request.code())
        .build();
  }

  /**
   * Term에 종속된 약관 본문 생성
   */
  public TermContent createTermContent(
      Term term,
      TermRegisterRequest request
  ) {
    return TermContent.builder()
        .term(term)
        .title(request.title())
        .content(request.content())
        .build();
  }

  /**
   * 약관 상세 조회 응답 변환
   */
  public TermSimpleResponse toTermSimpleResponse(
      Long termId,
      TermContent termContent
  ) {
    return TermSimpleResponse.builder()
        .termId(String.valueOf(termId))
        .title(termContent.getTitle())
        .content(termContent.getContent())
        .build();
  }
}
