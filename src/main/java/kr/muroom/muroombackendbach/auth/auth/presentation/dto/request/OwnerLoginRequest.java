package kr.muroom.muroombackendbach.auth.auth.presentation.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OwnerLoginRequest(
    @Email @NotBlank
    String email,
    
    @NotBlank
    String password
) {

}
