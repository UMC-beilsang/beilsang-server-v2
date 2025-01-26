package site.beilsang.beilsang_server_v2.global.oauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.beilsang.beilsang_server_v2.global.enums.Role;
import site.beilsang.beilsang_server_v2.global.jwt.JwtTokenProvider;
import site.beilsang.beilsang_server_v2.global.oauth.CustomOAuth2User;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2LoginSuccessHandler 로그인 성공");

        try {
            CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
            jwtTokenProvider.sendToken(response, oAuth2User);

        } catch (Exception e) {

        }
    }
}
