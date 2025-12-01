package kr.muroom.muroombackendbach.terms.domain.repository;

import static kr.muroom.muroombackendbach.terms.presentation.dto.TermDto.*;

import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import kr.muroom.muroombackendbach.terms.presentation.dto.TermDto;

import java.util.List;

public interface TermQueryRepository {

  List<TermsWithContentDto> findLatestTermsByRoleAndTypes(String role, List<TermsType> types);
}
