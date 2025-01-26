package site.beilsang.beilsang_server_v2.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import site.beilsang.beilsang_server_v2.global.jwt.JwtAuthFilter;
import site.beilsang.beilsang_server_v2.global.oauth.CustomOAuth2UserService;
import site.beilsang.beilsang_server_v2.global.oauth.handler.OAuth2LoginFailureHandler;
import site.beilsang.beilsang_server_v2.global.oauth.handler.OAuth2LoginSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    // 비밀번호 암&복호화를 위한 클래스
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
        MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);

        // white list
        MvcRequestMatcher[] permitWhiteList = {
                mvc.pattern("/oauth/**"),
                mvc.pattern("/favicon.ico"),
        };

        // http request 인증 설정
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(permitWhiteList).permitAll()
                .anyRequest().authenticated()
        );

        http.oauth2Login(oauth -> oauth
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService))
                .redirectionEndpoint(redirection -> redirection // 기본 리다이렉션 경로 변경
                        .baseUri("/oauth/redirect/*"))
                .successHandler(oAuth2LoginSuccessHandler) // oauth2 로그인 과정 인증
                .failureHandler(oAuth2LoginFailureHandler) // oauth2 로그인 과정 인증 실패
        );

        // jwt 방식 사용 -> 아래의 4개 미사용 설정
        http.httpBasic(AbstractHttpConfigurer::disable); // jwt 토큰(Bearer 방식) 사용하기 위해 httpBasic disable
        http.formLogin(AbstractHttpConfigurer::disable); // oauth2만 사용하기 때문에 diable
        http.logout(AbstractHttpConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);
        http.sessionManagement(session -> session // 세션을 사용하지 않기 때문에 stateless로 설정
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 토큰 검증 filter
        // header로부터 전달받는 토큰 검사, valid하면 authentication에 user 등록
        // UsernamePasswordAuthenticationFilter 전 jwtAuthFilter 실행
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // jwt를 포함한 exception handler
        http.exceptionHandling(conf -> conf
                // 인증 예외 처리
                .authenticationEntryPoint(customAuthenticationEntryPointHandler)
                // 인가 예외 처리
                .accessDeniedHandler(customAccessDeniedHandler));
        return http.build();
    }
}
