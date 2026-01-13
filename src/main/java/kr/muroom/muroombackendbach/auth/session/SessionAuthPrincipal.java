package kr.muroom.muroombackendbach.auth.session;

import kr.muroom.muroombackendbach.auth.auth.domain.entity.UserType;

public record SessionAuthPrincipal(UserType userType, Long userId) {

}
