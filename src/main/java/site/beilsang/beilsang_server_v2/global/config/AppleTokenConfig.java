package site.beilsang.beilsang_server_v2.global.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.feign.AppleClient;
import site.beilsang.beilsang_server_v2.global.oauth.dto.ApplePublicKeyRes;
import site.beilsang.beilsang_server_v2.global.oauth.dto.AppleRevokeReq;
import site.beilsang.beilsang_server_v2.global.oauth.dto.AppleTokenRes;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.*;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode.*;

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

    @Value("${apple.client-id}")
    private String clientId;

    @Value("${apple.key-id}")
    private String keyId;

    @Value("${apple.team-id}")
    private String teamId;

    @Value("${apple.private-key}")
    private String privateKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AppleClient appleClient;
    private static final String REFRESH_TOKEN = "refresh_token";

    // ==================== Client Secret 생성 ====================

    /**
     * Apple Client Secret (JWT) 생성
     * Apple API 호출 시 인증에 사용되는 JWT 토큰 생성
     *
     * @return Apple Client Secret JWT
     */
    public String generateClientSecret() {
        try {

            PrivateKey privateKey = getPrivateKey();
            Date now = new Date();
            Date expiration = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
            return Jwts.builder()
                .setHeaderParam("kid", keyId)
                .setHeaderParam("alg", "ES256")
                .setIssuer(teamId)
                .setAudience("https://appleid.apple.com")
                .setSubject(clientId)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();

        } catch (Exception e) {
            log.error("Failed to generate Apple client secret", e);
            throw new BaseException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Apple Private Key 로드
     * .p8 파일 내용을 PrivateKey 객체로 변환
     *
     * @return PrivateKey
     */
    private PrivateKey getPrivateKey() {

        try {
            Reader pemReader = new StringReader(privateKey.replace("\\n", "\n"));
            PEMParser pemParser = new PEMParser(pemReader);
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
            return converter.getPrivateKey(object);
        } catch (IOException e) {
            log.error("Failed to load Apple Private Key", e);
            throw new BaseException(INTERNAL_SERVER_ERROR);
        }
    }

    // ==================== Identity Token 검증 ====================

    /**
     * identityToken에서 사용자 정보 추출
     * 토큰을 검증한 후 필요한 사용자 정보만 Map 형태로 반환
     *
     * @param identityToken Apple identityToken
     * @return 사용자 정보 Map (sub: 고유 ID, email: 이메일 주소)
     */
    public Map<String, Object> getUserInfoFromToken(String identityToken) {
        Claims claims = verifyIdentityToken(identityToken);

        Map<String, Object> result = new HashMap<>();
        result.put("sub", claims.getSubject());

        String email = claims.get("email", String.class);
        if (email != null) {
            result.put("email", email);
        }
        return result;
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

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("identity Token is expired", e);
            throw new BaseException(EXPIRED_JWT);
        } catch (MalformedJwtException | SignatureException | UnsupportedJwtException e) {
            log.error("Invalid Apple identity token: {}", e.getMessage());
            throw new BaseException(INVALID_JWT);
        } catch (Exception e) {
            log.error("Failed to verify Apple identity token", e);
            throw new BaseException(INVALID_JWT);
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
        ApplePublicKeyRes response = appleClient.getAppleAuthPublicKeys();

        // 응답에서 keys 리스트 바로 가져오기
        List<ApplePublicKeyRes.ApplePublicKey> keys = response.getKeys();

        // kid에 해당하는 키 찾기
        for (ApplePublicKeyRes.ApplePublicKey key : keys) {
            if (kid.equals(key.getKid())) {
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
    private PublicKey createPublicKey(ApplePublicKeyRes.ApplePublicKey key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        String n = key.getN();
        String e = key.getE();

        byte[] nBytes = Base64.getUrlDecoder().decode(n);
        byte[] eBytes = Base64.getUrlDecoder().decode(e);

        BigInteger modulus = new BigInteger(1, nBytes);
        BigInteger exponent = new BigInteger(1, eBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(spec);
    }

    // ==================== Apple Token 발급/폐기 ====================

    public AppleTokenRes getAppleToken(String authorizationCode) {
        try {
            log.info("Requesting Apple token with authorization code");

            String clientSecret = generateClientSecret();

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("code", authorizationCode);
            body.add("grant_type", "authorization_code");

            AppleTokenRes response = appleClient.getAppleToken(body);
            if (response.error() != null || response.refreshToken() == null) {
                log.error("Failed to get Apple refresh token: {}", response.error());
                throw new BaseException(INTERNAL_SERVER_ERROR);
            }

            return response;

        } catch (FeignException.BadRequest e) {

            String body = e.contentUTF8();
            log.error("Apple returned BadRequest: {}", body);

            if (body.contains("invalid_grant")) {
                throw new BaseException(INVALID_APPLE_AUTHORIZATION_CODE);
            }

            throw new BaseException(INTERNAL_SERVER_ERROR);

        } catch (Exception e) {
            log.error("Failed to get Apple token", e);
            log.error(e.getMessage());
            throw new BaseException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Apple Refresh Token 폐기 (연동 해제)
     * 사용자의 Apple 계정 연동을 완전히 해제
     *
     * @param refreshToken Apple refresh token
     */
    public void revoke(String refreshToken) {
        try {
            log.info("Revoking Apple refresh token");

            AppleRevokeReq revokeRequest = AppleRevokeReq.builder()
                .clientId(clientId)
                .clientSecret(generateClientSecret())
                .token(refreshToken)
                .tokenTypeHint(REFRESH_TOKEN)
                .build();

            appleClient.revoke(revokeRequest);
            log.info("Apple token revoked successfully");

        } catch (Exception e) {
            log.error("Failed to revoke Apple token", e);
            throw new BaseException(APPLE_REVOKE_FAILED);
        }
    }
}
