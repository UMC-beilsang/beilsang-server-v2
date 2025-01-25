package site.beilsang.beilsang_server_v2.global.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.global.enums.Role;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // HTTP 요청 헤더에서 access token을 추출
        String token = extractToken(request);

        logger.info("1번-----------------");
        // access token이 존재하지 않거나 토큰이 유효하지 않으면 401 코드 반환
        if (!jwtTokenProvider.validateToken(token) || !StringUtils.hasText(token)) {
            //TODO - 아래처럼 sendError하면 토큰이 필요 없는 도메인에서 접근 안됨
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            //토큰이 없을 경우
            doFilter(request,response,filterChain);
            return;
        }
        setSecurityContextHolder(token);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.equals("/form") || path.equals("/favicon.ico") || path.equals("/oauth/**");
    }

    /**
     * Header에서 token 추출
     *
     * @param request
     * @return
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 토큰 속 정보를 바탕으로 유저를 SecurityContextHolder에 저장
     *
     * @param token
     */
    private void setSecurityContextHolder(String token) {
        String socialId = jwtTokenProvider.getSocialId(token);
        //TODO
        Member member = memberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new RuntimeException());

        // 인증 토큰을 받아 SecurityContext에 저장
        SecurityContextHolder.getContext().setAuthentication(getUserAuth(member));
    }

    /**
     * 멤버 정보를 바탕으로 인증 토큰 생성
     *
     * @param member
     * @return UsernamePasswordAuthenticationToken
     */
    private UsernamePasswordAuthenticationToken getUserAuth(Member member) {
        return new UsernamePasswordAuthenticationToken(
                member.getId(),
                member.getSocialId(),
                Collections.singleton(new SimpleGrantedAuthority(Role.USER.getRole()))
        );
    }
}
