package kr.muroom.muroombackendbach.user.presentation;

import kr.muroom.muroombackendbach.user.application.MusicianService;
import kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/musician")
@RequiredArgsConstructor
public class MusicianController {

    private final MusicianService musicianService;

    @PostMapping
    public void registerMusician(@RequestBody MusicianDto.MusicianSignUpDto musicianSignUpRequest) {
        musicianService.registerMusician(musicianSignUpRequest);
    }


}
