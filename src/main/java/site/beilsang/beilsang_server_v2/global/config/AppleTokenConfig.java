package site.beilsang.beilsang_server_v2.global.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;

/**
 * Apple 로그인 시 JWT 토큰 생성 및 검증을 담당하는 컴포넌트
 * - Apple client secret 생성
 * - Apple identity token 검증
 * - 사용자 정보 추출
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppleTokenConfig {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * identityToken에서 사용자 정보 추출
     * 토큰을 검증한 후 필요한 사용자 정보만 Map 형태로 반환
     *
     * @param identityToken Apple identityToken
     * @return 사용자 정보 Map (sub: 고유 ID, email: 이메일 주소)
     */
    public Map<String, Object> getUserInfoFromToken(String identityToken) {
        Claims claims = verifyIdentityToken(identityToken);

        return Map.of(
            "sub", claims.getSubject(),
            "email", claims.get("email", String.class));
    }

    /**
     * 애플 identityToken 검증 및 사용자 정보 추출
     * 1. JWT 헤더에서 kid 추출
     * 2. Apple 공개키 서버에서 해당 kid의 공개키 조회
     * 3. 공개키로 JWT 서명 검증
     * 4. 검증된 JWT의 Claims 반환
     *
     * @param identityToken 네이티브 앱에서 전달받은 identityToken
     * @return Claims 검증된 사용자 정보
     * @throws RuntimeException 토큰 검증 실패 시
     */
    public Claims verifyIdentityToken(String identityToken) {
        try {
            // JWT 헤더에서 kid 추출
            String[] chunks = identityToken.split("\\.");
            String header = new String(Base64.getUrlDecoder().decode(chunks[0]));
            JsonNode headerJson = objectMapper.readTree(header);
            String kid = headerJson.get("kid").asText();

            // Apple 공개키 가져오기
            PublicKey publicKey = getApplePublicKey(kid);

            // JWT 검증 및 Claims 추출
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(identityToken)
                    .getBody();

        } catch (Exception e) {
            log.error("Failed to verify Apple identity token", e);
            throw new RuntimeException("Invalid Apple identity token", e);
        }
    }

    /**
     * Apple 공개키 가져오기
     *
     * @param kid Key ID
     * @return PublicKey
     */
    private PublicKey getApplePublicKey(String kid) throws Exception {
        // Apple 공개키 엔드포인트에서 키 정보 가져오기
        String appleKeysUrl = "https://appleid.apple.com/auth/keys";
        ResponseEntity<String> response = restTemplate.getForEntity(appleKeysUrl, String.class);

        JsonNode keysJson = objectMapper.readTree(response.getBody());
        JsonNode keys = keysJson.get("keys");

        // kid에 해당하는 키 찾기
        for (JsonNode key : keys) {
            if (kid.equals(key.get("kid").asText())) {
                return createPublicKey(key);
            }
        }

        throw new RuntimeException("Apple public key not found for kid: " + kid);
    }

    /**
     * JSON Web Key를 PublicKey로 변환
     *
     * @param key JWK
     * @return PublicKey
     */
    private PublicKey createPublicKey(JsonNode key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String n = key.get("n").asText();
        String e = key.get("e").asText();

        byte[] nBytes = Base64.getUrlDecoder().decode(n);
        byte[] eBytes = Base64.getUrlDecoder().decode(e);

        BigInteger modulus = new BigInteger(1, nBytes);
        BigInteger exponent = new BigInteger(1, eBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(spec);
    }
}
