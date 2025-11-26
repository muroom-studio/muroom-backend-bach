package kr.muroom.muroombackendbach.auth.oauth.handler.dto;

public record LoginErrorResponse(
        String code,
        String message
) {
    public static LoginErrorResponse of(String code, String message) {
        return new LoginErrorResponse(code, message);
    }
}