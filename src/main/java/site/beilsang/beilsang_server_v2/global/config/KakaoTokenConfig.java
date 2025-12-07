package site.beilsang.beilsang_server_v2.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.feign.KakaoAuthClient;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoTokenConfig {
    private final KakaoAuthClient kakaoAuthClient;

    @Value("${kakao.app-key}")
    private String appKey;

    private final ObjectMapper objectMapper;

    public Map<String, Object> validateAndExtractUserInfo(String idToken) {
        try {
            // JJWT로 파싱 및 검증
            Jws<Claims> jws = Jwts.parserBuilder()
                .setSigningKeyResolver(new KakaoSigningKeyResolver())
                .requireIssuer("https://kauth.kakao.com")
                .requireAudience(appKey)
                .build()
                .parseClaimsJws(idToken);

            Claims claims = jws.getBody();

            // 사용자 정보 추출
            Map<String, Object> userInfo = new HashMap<>();
            Long socialId = Long.valueOf(claims.getSubject());
            userInfo.put("id", socialId);

            String email = claims.get("email", String.class);
            if (email != null) {
                Map<String, Object> kakaoAccount = new HashMap<>();
                kakaoAccount.put("email", email);
                userInfo.put("kakao_account", kakaoAccount);
            }

            return userInfo;

        } catch (JwtException e) {
            log.error("Invalid ID Token", e);
            throw new BaseException(INVALID_JWT);
        } catch (Exception e) {
            log.error("Kakao login failed", e);
            throw new BaseException(INVALID_JWT);
        }
    }

    // JWKS에서 공개키 가져오는 Resolver
    private class KakaoSigningKeyResolver extends SigningKeyResolverAdapter {
        @Override
        public Key resolveSigningKey(JwsHeader header, Claims claims) {
            // JWKS에서 kid에 해당하는 공개키 반환
            // (위의 getPublicKey 로직 재사용)
            return getPublicKey(header.getKeyId());
        }
    }

    /**
     * JWKS에서 공개키 가져오기
     */
    private PublicKey getPublicKey(String kid) {

        // JWKS 가져오기
        try {
            String jwksJson = kakaoAuthClient.getJwks();

            Map<String, Object> jwks = objectMapper.readValue(jwksJson, Map.class);
            List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");

            // kid로 키 찾기
            for (Map<String, Object> key : keys) {
                if (kid.equals(key.get("kid"))) {
                    return buildPublicKey(key);
                }
            }

            throw new BaseException(INVALID_JWT);

        } catch (Exception e) {
            log.error("Failed to fetch JWKS", e);
            throw new BaseException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * JWK에서 RSA 공개키 생성
     */
    private PublicKey buildPublicKey(Map<String, Object> jwk) {
        try {
            String n = (String) jwk.get("n"); // modulus
            String e = (String) jwk.get("e"); // exponent

            byte[] nBytes = Base64.getUrlDecoder().decode(n);
            byte[] eBytes = Base64.getUrlDecoder().decode(e);

            BigInteger modulus = new BigInteger(1, nBytes);
            BigInteger exponent = new BigInteger(1, eBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");

            return factory.generatePublic(spec);

        } catch (Exception e) {
            log.error("Failed to build public key", e);
            throw new BaseException(INTERNAL_SERVER_ERROR);
        }
    }
}
