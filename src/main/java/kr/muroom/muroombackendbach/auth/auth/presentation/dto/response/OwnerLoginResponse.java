package kr.muroom.muroombackendbach.auth.auth.presentation.dto.response;

import lombok.Builder;

@Builder
public record OwnerLoginResponse(
    String accessToken,
    String refreshToken,
    String ownerId
) {

  public static OwnerLoginResponse of(String accessToken, String refreshToken, Long ownerId) {
    return new OwnerLoginResponse(accessToken, refreshToken, String.valueOf(ownerId));
  }
}
