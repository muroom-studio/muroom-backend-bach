package kr.muroom.muroombackendbach.terms.presentation;

import kr.muroom.muroombackendbach.terms.application.TermService;
import kr.muroom.muroombackendbach.terms.domain.entity.TermsType;
import kr.muroom.muroombackendbach.terms.presentation.dto.TermDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;

    @GetMapping("/musician")
    public ResponseEntity<List<TermDto.TermsWithContentDto>> getMusicianTerms(@RequestParam(required = false) List<TermsType> types) {
        return ResponseEntity.ok().body(termService.getTermsMusicianByType(types));
    }

    @GetMapping("/owner")
    public ResponseEntity<List<TermDto.TermsWithContentDto>> getOwnerTerms(@RequestParam(required = false) List<TermsType> types) {
        return ResponseEntity.ok().body(termService.getTermsOwnerByType(types));
    }


}
