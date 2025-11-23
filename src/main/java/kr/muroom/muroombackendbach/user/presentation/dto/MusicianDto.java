package kr.muroom.muroombackendbach.user.presentation.dto;


import java.time.LocalDate;
import java.util.List;

public final class MusicianDto {

    private MusicianDto() {
    }

    public record MusicianSignUpDto(
            String name,
            LocalDate birthdate,
            String phoneNumber,
            String nickname,
            List<Long> termIds
    ) {}
}
