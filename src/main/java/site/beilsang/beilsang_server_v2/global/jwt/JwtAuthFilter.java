package site.beilsang.beilsang_server_v2.global.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

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
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.enums.Role;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (!StringUtils.hasText(token)) {
            request.setAttribute("exception", "NO_JWT");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 토큰 검증
            jwtTokenProvider.validateToken(token);

            // SecurityContext 설정 (회원이 없으면 설정 안 함)
            setSecurityContextHolder(token);

        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token");
            request.setAttribute("exception", "EXPIRED_JWT");
        } catch (BaseException e) {
            log.warn("Member not found (deleted): {}", e.getMessage());
            request.setAttribute("exception", "MEMBER_DELETED");
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            request.setAttribute("exception", "INVALID_JWT");
        }

        filterChain.doFilter(request, response);
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
        String socialId = jwtTokenProvider.getClaimFromToken(token, "socialId");
        String email = jwtTokenProvider.getClaimFromToken(token, "email");
        Member member = memberRepository.findBySocialIdAndEmail(socialId, email)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        // 인증 토큰을 받아 SecurityContext에 저장
        SecurityContextHolder.getContext().setAuthentication(getUserAuth(member));
    }
    /**
     * 멤버 정보를 바탕으로 인증 토큰 생성, Controller에서 Authentication.getPrincipal로 값 받아올 수 있음
     *
     * @param member
     * @return UsernamePasswordAuthenticationToken
     */
    private UsernamePasswordAuthenticationToken getUserAuth(Member member) {
        return new UsernamePasswordAuthenticationToken(member.getId(), //member가 아닌 memberId를 넣어 최소한의 정보만 갖도록 설정
            member.getSocialId(), Collections.singleton(new SimpleGrantedAuthority(Role.USER.getRole())));
    }
}
