package kr.muroom.muroombackendbach.auth.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.muroom.muroombackendbach.auth.jwt.JwtTokenProvider;
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

        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();

        // 회원가입이 필요한 유저
        if (principal instanceof UnregisteredOAuth2User unregistered) {
            String signupToken = jwtTokenProvider.createSignupToken(
                    unregistered.provider(),
                    unregistered.providerId()
            );

            body.put("status", "SIGNUP_REQUIRED");
            body.put("token", signupToken);
            body.put("provider", unregistered.provider());

            response.getWriter().write(objectMapper.writeValueAsString(body));
            response.getWriter().flush();
            return;
        }

        // 이미 회원가입한 유저
        if (principal instanceof CustomOAuth2User user) {
            Long musicianId = user.musicianId();
            String token = jwtTokenProvider.createToken(musicianId);

            body.put("status", "SUCCESS");
            body.put("token", token);
            body.put("musicianId", musicianId);
            body.put("provider", user.provider());

            response.getWriter().write(objectMapper.writeValueAsString(body));
            response.getWriter().flush();
            return;
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }
}



