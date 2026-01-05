package kr.muroom.muroombackendbach.terms.domain.repository;

import java.util.List;
import kr.muroom.muroombackendbach.terms.domain.entity.TargetRole;
import kr.muroom.muroombackendbach.terms.presentation.dto.response.TermDetailResponse;

public interface TermQueryRepository {

  List<TermDetailResponse> findLatestTermsByRoleAndTypes(TargetRole role);
}
