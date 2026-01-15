package kr.muroom.muroombackendbach.auth.auth.presentation.dto.response;

import lombok.Builder;

@Builder
public record OwnerLoginResponse(
    String ownerId
) {

}
