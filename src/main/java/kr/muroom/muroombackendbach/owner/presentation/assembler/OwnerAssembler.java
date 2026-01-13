package kr.muroom.muroombackendbach.owner.presentation.assembler;

import kr.muroom.muroombackendbach.owner.domain.entity.Owner;
import kr.muroom.muroombackendbach.owner.presentation.dto.response.OwnerProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class OwnerAssembler {

  public OwnerProfileResponse toOwnerProfileResponse(Owner owner) {
    return OwnerProfileResponse.builder()
        .name(owner.getName())
        .phone(owner.getPhoneNumber())
        .nickname(owner.getNickname())
        .ownerId(String.valueOf(owner.getId()))
        .build();
  }
}
