package kr.muroom.muroombackendbach.user.presentation;

import kr.muroom.muroombackendbach.user.application.MusicianService;
import kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/musician")
@RequiredArgsConstructor
public class MusicianController {

    private final MusicianService musicianService;

    @PostMapping
    public ResponseEntity<Object> registerMusician(@RequestBody MusicianDto.MusicianSignUpDto musicianSignUpRequest) {
        musicianService.registerMusician(musicianSignUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


}
