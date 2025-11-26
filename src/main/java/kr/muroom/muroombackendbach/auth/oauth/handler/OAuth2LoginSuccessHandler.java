package kr.muroom.muroombackendbach.auth.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider;
import kr.muroom.muroombackendbach.auth.oauth.handler.dto.LoginErrorResponse;
import kr.muroom.muroombackendbach.auth.oauth.handler.dto.OAuth2LoginSuccessResponse;
import kr.muroom.muroombackendbach.auth.oauth.handler.dto.OAuth2SignupResponse;
import kr.muroom.muroombackendbach.auth.oauth.user.CustomOAuth2User;
import kr.muroom.muroombackendbach.auth.oauth.user.UnregisteredOAuth2User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Object principal = authentication.getPrincipal();

        // 회원가입이 필요한 유저
        if (principal instanceof UnregisteredOAuth2User unregistered) {
            String signupToken = jwtTokenProvider.createSignupToken(
                    unregistered.provider(),
                    unregistered.providerId()
            );

            OAuth2SignupResponse body = OAuth2SignupResponse.of(signupToken, unregistered.provider());
            writeJsonResponse(response, HttpServletResponse.SC_OK, body);
            return;
        }

        // 이미 회원가입한 유저
        if (principal instanceof CustomOAuth2User user) {
            Long musicianId = user.musicianId();
            String token = jwtTokenProvider.createToken(musicianId);

            OAuth2LoginSuccessResponse body =
                    OAuth2LoginSuccessResponse.of(token, musicianId, user.provider());
            writeJsonResponse(response, HttpServletResponse.SC_OK, body);
            return;
        }

        LoginErrorResponse errorBody = LoginErrorResponse.of(
                "UNEXPECTED_PRINCIPAL_TYPE",
                "예상하지 못한 인증 주체 타입입니다."
        );
        writeJsonResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorBody);
    }

    private void writeJsonResponse(HttpServletResponse response, int status, Object body) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.getWriter().flush();
    }
}






