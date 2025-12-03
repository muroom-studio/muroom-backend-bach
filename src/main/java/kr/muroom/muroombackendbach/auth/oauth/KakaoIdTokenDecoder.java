package kr.muroom.muroombackendbach.auth.oauth;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.JWT;
import org.springframework.stereotype.Component;

@Component
public class KakaoIdTokenDecoder {

  public KakaoIdTokenPayload decode(String idToken) {
    DecodedJWT jwt = JWT.decode(idToken);

    KakaoIdTokenPayload payload = new KakaoIdTokenPayload();
    payload.setAud(jwt.getClaim("aud").asString());
    payload.setSub(jwt.getSubject());
    payload.setAuth_time(jwt.getClaim("auth_time").asLong());
    payload.setIss(jwt.getIssuer());
    payload.setExp(jwt.getExpiresAt().getTime());
    payload.setIat(jwt.getIssuedAt().getTime());
    payload.setNickname(jwt.getClaim("nickname").asString());
    payload.setPicture(jwt.getClaim("picture").asString());
    payload.setEmail(jwt.getClaim("email").asString());

    return payload;
  }
}

