package site.beilsang.beilsang_server_v2.global.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import site.beilsang.beilsang_server_v2.global.oauth.dto.ApplePublicKeyRes;
import site.beilsang.beilsang_server_v2.global.oauth.dto.AppleRevokeReq;
import site.beilsang.beilsang_server_v2.global.oauth.dto.AppleTokenRes;


@FeignClient(name = "appleClient", url = "https://appleid.apple.com/auth")
public interface AppleClient {

    String CONTENT_TYPE = "application/x-www-form-urlencoded";

    /**
     * Apple ID 공개키 조회
     * - identityToken(JWT) 서명 검증에 사용(로그인)
     * - Apple에서 제공하는 RSA 공개키 목록을 가져옴
     */
    @GetMapping(value = "/keys")
    ApplePublicKeyRes getAppleAuthPublicKeys();

    /**
     * Apple Token 발급/재발급 요청
     * - authorization_code → access_token / refresh_token 교환
     * - refresh_token → 새 access_token 발급
     */
    @PostMapping(value = "/token", consumes = CONTENT_TYPE)
    AppleTokenRes getAppleToken(MultiValueMap<String, String> form);

    /**
     * Apple Refresh Token 폐기(연동 해제)
     * - 사용자의 Apple 계정 연동을 완전히 해제
     * - 일반적으로 애플 회원 탈퇴 시 사용
     */
    @PostMapping(value = "/revoke", consumes = CONTENT_TYPE)
    void revoke(AppleRevokeReq request);
}
