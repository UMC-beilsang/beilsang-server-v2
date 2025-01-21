package site.beilsang.beilsang_server_v2.global.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 14;  // 14일

    @Value("${jwt.secret-key}")
    private String secretKey; // jwt 시크릿 키
    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    /**
     * access token 생성
     *
     * @param socialId
     * @return accessToken
     */
    public String createAccessToken(String socialId) {
        return createToken(socialId, ACCESS_TOKEN_EXPIRE_TIME);
    }

    /**
     * refresh token 생성
     *
     * @return refresh token
     */
    public String createRefreshToken() {
        byte[] array = new byte[7];
        new Random().nextBytes(array);
        String generatedString = new String(array, StandardCharsets.UTF_8);
        return createToken(generatedString, REFRESH_TOKEN_EXPIRE_TIME);
    }


    /**
     * 만료 기한 별 token 생성
     *
     * @param socialId
     * @param expireTime
     */
    private String createToken(String socialId, long expireTime) {
        Claims claims = Jwts.claims().setSubject(socialId);
        Date now = new Date();
        Date validity = new Date(now.getTime() + expireTime);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
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
     * claim에서 socialId 추출
     *
     * @param token
     */
    public String getSocialId(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (JwtException e) {
            //TODO
//            throw new ErrorHandler(ErrorStatus.UNAUTHORIZED);
            throw new RuntimeException();
        }
    }


}
