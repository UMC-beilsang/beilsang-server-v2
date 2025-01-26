package site.beilsang.beilsang_server_v2.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import site.beilsang.beilsang_server_v2.domain.member.dto.MemberAssembler;
import site.beilsang.beilsang_server_v2.domain.member.dto.MemberLoginResDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.global.oauth.CustomOAuth2User;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class JwtTokenProvider {

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 14;  // 14일

    @Value("${jwt.secret-key}")
    private String secretKey; // jwt 시크릿 키
    private static SecretKey key;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * access token 생성
     *
     * @param socialId
     * @param email
     * @return accessToken
     */
    public String createAccessToken(String socialId, String email) {
        return createToken(socialId, email, ACCESS_TOKEN_EXPIRE_TIME);
    }

    /**
     * refresh token 생성
     *
     * @return refresh token
     */
    public String createRefreshToken(String socialId, String email) {
        String refreshToken = createToken(socialId, email, REFRESH_TOKEN_EXPIRE_TIME);

        Member member = memberRepository.findBySocialIdAndEmail(socialId, email)
                .orElseThrow();
        member.setRefreshToken(refreshToken);
        memberRepository.save(member);

        return refreshToken;
    }


    /**
     * 만료 기한 별 token 생성
     *
     * @param socialId
     * @param expireTime
     */
    private String createToken(String socialId, String email, long expireTime) {
        Claims claims = Jwts.claims()
                .setSubject(email);
        claims.put("socialId", socialId);
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireTime);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 토큰 유효성 검사
     *
     * @param token
     */
    public Boolean validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return !claimsJws.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    /**
     * 토큰에서 Claim을 통해 원하는 값 추출
     *
     * @param token    accessToken
     * @param claimKey Claim에서 추출하려는 값
     * @return 값
     */
    public String getClaimFromToken(String token, String claimKey) {
        try {
            if ("email".equals(claimKey)) {
                return Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject();
            }
            if ("socialId".equals(claimKey)) {
                return (String) Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .get(claimKey);
            }
            return null;
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (JwtException e) {
            //TODO
//        throw new ErrorHandler(ErrorStatus.UNAUTHORIZED);
            throw new RuntimeException();
        }
    }

    public void sendToken(HttpServletResponse response, CustomOAuth2User oAuth2User) throws IOException {
        String accessToken = createAccessToken(oAuth2User.getSocialId(), oAuth2User.getEmail());
        String refreshToken = createRefreshToken(oAuth2User.getSocialId(), oAuth2User.getEmail());
        MemberLoginResDTO memberLoginResDTO = MemberAssembler.toMemberLoginResDTO(accessToken, refreshToken, oAuth2User.getRole());
        response.getWriter().write(objectMapper.writeValueAsString(memberLoginResDTO));
    }
}
