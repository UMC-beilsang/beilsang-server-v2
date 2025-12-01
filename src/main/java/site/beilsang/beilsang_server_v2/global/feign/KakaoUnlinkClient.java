package site.beilsang.beilsang_server_v2.global.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

//KakaoUnlinkClient
@FeignClient(name = "KakaoUnlinkClient", url = "https://kapi.kakao.com")
public interface KakaoUnlinkClient {

    @PostMapping(value = "/v1/user/logout", consumes = "application/x-www-form-urlencoded")
    void logoutUser(
        @RequestHeader("Authorization") String authorization,
        @RequestParam(name = "target_id_type", defaultValue = "user_id") String targetIdType,
        @RequestParam("target_id") Long targetId
    );

    @PostMapping(value = "/v1/user/unlink", consumes = "application/x-www-form-urlencoded")
    void unlinkUser(
        @RequestHeader("Authorization") String authorization,
        @RequestParam(name = "target_id_type", defaultValue = "user_id") String targetIdType,
        @RequestParam("target_id") Long targetId
    );
}
