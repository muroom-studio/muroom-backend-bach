package kr.muroom.muroombackendbach.auth.oauth.handler.dto;

public record OAuth2SignupResponse(
        String status,
        String token,
        String provider
) {
    public static OAuth2SignupResponse of(String token, String provider) {
        return new OAuth2SignupResponse("SIGNUP_REQUIRED", token, provider);
    }
}

