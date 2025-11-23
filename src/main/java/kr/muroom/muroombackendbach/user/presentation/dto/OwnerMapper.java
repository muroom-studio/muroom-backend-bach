package kr.muroom.muroombackendbach.user.presentation.dto;

import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import org.springframework.stereotype.Component;

@Component
public class OwnerMapper {
    public Owner toEntity(OwnerDto.OwnerSignUpDto dto) {
        return Owner.of(
                dto.name(),
                dto.birthdate(),
                dto.phoneNumber(),
                dto.nickname()
        );
    }
}
