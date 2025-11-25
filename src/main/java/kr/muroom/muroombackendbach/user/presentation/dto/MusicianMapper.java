package kr.muroom.muroombackendbach.user.presentation.dto;

import kr.muroom.muroombackendbach.user.domain.entity.Musician;
import org.springframework.stereotype.Component;

@Component
public class MusicianMapper {
    public Musician toEntity(MusicianDto.MusicianSignUpDto dto) {
        return Musician.of(
                dto.name(),
                dto.birthdate(),
                dto.phoneNumber(),
                dto.nickname()
        );
    }
}