package site.beilsang.beilsang_server_v2.global.oauth.dto;

import lombok.Builder;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.global.enums.Provider;

import java.util.Map;

@Getter
@Builder
public class OAuthAttributes {
    /**
     * 회원 정보를 담는 DTO
     * 소셜 타입 별로 받는 데이터를 분기 처리한다
     */

    private String nameAttributesKey; // OAuth2 로그인 진행 시 키가 되는 필드 값, PK와 같은 의미
    private OAuth2UserInfo oAuth2UserInfo; // 소셜 타입별 로그인 유저 정보

    public static OAuthAttributes of(Provider provider, String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .nameAttributesKey(userNameAttributeName)
                .oAuth2UserInfo(getOAuth2UserInfoByProvider(provider, attributes))
                .build();
    }

    private static OAuth2UserInfo getOAuth2UserInfoByProvider(Provider provider, Map<String, Object> attributes) {
        if (provider.equals(Provider.KAKAO)) {
            return new KakaoUserInfo(attributes);
        }

        return null;
    }


}
