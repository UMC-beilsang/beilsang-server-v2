package site.beilsang.beilsang_server_v2.global.oauth.dto;

import java.util.Map;

public class AppleUserInfo extends OAuth2UserInfo {
    public AppleUserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }
}
