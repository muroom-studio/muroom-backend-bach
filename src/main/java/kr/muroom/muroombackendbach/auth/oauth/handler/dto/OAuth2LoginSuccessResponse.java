package kr.muroom.muroombackendbach.auth.oauth.handler.dto;

public record OAuth2LoginSuccessResponse(
        String status,
        String token,
        Long musicianId,
        String provider
) {
    public static OAuth2LoginSuccessResponse of(String token, Long musicianId, String provider) {
        return new OAuth2LoginSuccessResponse("SUCCESS", token, musicianId, provider);
    }
}

