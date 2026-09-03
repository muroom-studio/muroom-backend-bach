package kr.muroom.muroombackendbach.owner.presentation.dto.response;

public record OwnerSignupResponse(
    String accessToken,
    String refreshToken,
    String ownerId
) {

}
