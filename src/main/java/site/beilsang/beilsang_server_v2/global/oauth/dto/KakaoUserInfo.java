package site.beilsang.beilsang_server_v2.global.oauth.dto;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class KakaoUserInfo extends OAuth2UserInfo {

    public KakaoUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        Long socialId = (Long) attributes.get("id");
        //카카오는 id를 Long형태로 전달
        return String.valueOf(socialId);
    }

    public String getEmail() {
        Map<String, Object> response = (Map<String, Object>) attributes.get("kakao_account");
        if (response == null) {
            return null;
        }
        return (String) response.get("email");
    }
}
