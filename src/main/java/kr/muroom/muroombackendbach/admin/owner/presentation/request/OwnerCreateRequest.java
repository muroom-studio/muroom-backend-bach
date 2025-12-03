package kr.muroom.muroombackendbach.admin.owner.presentation.request;

import jakarta.validation.constraints.NotNull;

// UNVERIFIED
public record OwnerCreateRequest(
    @NotNull
    String nickname,

    @NotNull
    String phoneNumber,

    Integer experienceYears
) {

}
