package kr.muroom.muroombackendbach.user.presentation.dto;

import java.time.LocalDate;
import java.util.List;

public class OwnerDto {
    private OwnerDto() {
    }

    public record OwnerSignUpDto(
            String name,
            LocalDate birthdate,
            String phoneNumber,
            String nickname,
            List<Long> termIds
    ) {}
}
