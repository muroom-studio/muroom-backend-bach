package kr.muroom.muroombackendbach.terms.application;

import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import kr.muroom.muroombackendbach.terms.domain.repository.TermRepository;
import kr.muroom.muroombackendbach.terms.presentation.dto.TermDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TermService {

    private final TermRepository termRepository;

    public List<TermDto.TermsWithContentDto> getTermsMusicianByType(List<TermsType> types) {
        return termRepository.findLatestTermsByRoleAndTypes("MUSICIAN", types);
    }

    public List<TermDto.TermsWithContentDto> getTermsOwnerByType(List<TermsType> types) {
        return termRepository.findLatestTermsByRoleAndTypes("OWNER", types);
    }
}
