package kr.muroom.muroombackendbach.user.presentation.dto;

import kr.muroom.muroombackendbach.user.domain.entity.Owner;
import kr.muroom.muroombackendbach.user.domain.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DTO -> Entity 로 변환하는 매퍼 메서드를 모아놓은 클래스입니다.
 */
@Component
@RequiredArgsConstructor
public class OwnerMapper {

  private final PasswordEncoder passwordEncoder;

  public Owner toEntity(OwnerDto.OwnerSignUpDto dto) {
    return Owner.builder()
        .email(dto.email())
        .password(passwordEncoder.encode(dto.password()))
        .name(dto.name())
        .birthdate(dto.birthdate())
        .phoneNumber(dto.phoneNumber())
        .nickname(dto.nickname())
        .status(UserStatus.ACTIVE)
        .build();
  }
}
