package kr.muroom.muroombackendbach.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import kr.muroom.muroombackendbach.common.presentation.response.ApiResponse;
import kr.muroom.muroombackendbach.user.application.MusicianService;
import kr.muroom.muroombackendbach.user.presentation.dto.MusicianDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/musician")
@RequiredArgsConstructor
@Tag(name = "뮤지션 API", description = "뮤지션 관련 API")
public class MusicianController {

    private final MusicianService musicianService;

    @Operation(summary = "뮤지션 회원가입", description = "뮤지션 회원 정보를 등록합니다." )
    @PostMapping("/register")
    public ApiResponse<MusicianDto.MusicianSignUpResponse> registerMusician(@RequestBody MusicianDto.MusicianSignUpDto musicianSignUpRequest) {
        MusicianDto.MusicianSignUpResponse response =
                musicianService.registerMusician(musicianSignUpRequest);
        return ApiResponse.created(response);
    }
}
