package kr.muroom.muroombackendbach.terms.domain.repository;

import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import kr.muroom.muroombackendbach.terms.domain.presentation.dto.TermDto;

import java.util.List;

public interface TermQueryRepository {
    List<TermDto.TermsWithContentDto> findLatestTermsByRoleAndTypes(String role, List<TermsType> types);
}
