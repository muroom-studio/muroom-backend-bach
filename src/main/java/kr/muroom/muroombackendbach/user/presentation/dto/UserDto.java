package kr.muroom.muroombackendbach.user.presentation.dto;

public class UserDto {
    private UserDto() {}

    public record NicknameCheckResponse(
       boolean available
    ) {}
}
