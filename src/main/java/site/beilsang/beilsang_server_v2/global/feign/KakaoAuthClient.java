package site.beilsang.beilsang_server_v2.global.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "kakaoAuthClient", url = "https://kauth.kakao.com")
public interface KakaoAuthClient {

    @GetMapping("/.well-known/jwks.json")
    String getJwks();
}
