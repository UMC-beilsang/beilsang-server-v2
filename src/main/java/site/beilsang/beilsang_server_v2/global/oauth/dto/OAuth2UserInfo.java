package site.beilsang.beilsang_server_v2.global.oauth.dto;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public abstract class OAuth2UserInfo {
    //소셜 타입별 로그인 유저 정보

    protected Map<String, Object> attributes;

    /**
     * 서비스에 사용할 유저 정보들을 가져오는 메소드
     *
     * @return
     */
    // 동일인 식별 정보를 가져옴(아이디마다 고유하게 발급되는 값)
    // 네이버, 카카오 = id, 구글 = sub
    public abstract String getId();

    public abstract String getEmail();
}
