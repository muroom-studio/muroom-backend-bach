package kr.muroom.muroombackendbach.terms.domain.application;

import kr.muroom.muroombackendbach.terms.domain.entity.Terms;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import kr.muroom.muroombackendbach.terms.domain.presentation.dto.TermDto;
import kr.muroom.muroombackendbach.terms.domain.repository.TermQueryRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TermService {

    private final TermQueryRepositoryImpl termsRepository;

    public List<TermDto.TermsWithContentDto> getTermsMusicianByType(List<TermsType> types) {
        return termsRepository.findLatestTermsByRoleAndTypes("MUSICIAN", types);
    }

    public List<TermDto.TermsWithContentDto> getTermsOwnerByType(List<TermsType> types) {
        return termsRepository.findLatestTermsByRoleAndTypes("OWNER", types);
    }
}
