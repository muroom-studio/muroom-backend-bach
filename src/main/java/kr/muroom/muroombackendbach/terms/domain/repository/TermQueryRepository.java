package kr.muroom.muroombackendbach.terms.domain.repository;

import static kr.muroom.muroombackendbach.terms.presentation.dto.TermDto.TermsWithContentDto;

import java.util.List;
import kr.muroom.muroombackendbach.terms.domain.entity.TargetRole;

public interface TermQueryRepository {

  List<TermsWithContentDto> findLatestTermsByRoleAndTypes(TargetRole role);
}
