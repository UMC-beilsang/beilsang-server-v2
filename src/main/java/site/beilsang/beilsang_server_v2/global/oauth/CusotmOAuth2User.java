package site.beilsang.beilsang_server_v2.global.oauth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import site.beilsang.beilsang_server_v2.global.enums.Role;

import java.util.Collection;
import java.util.Map;

@Getter
public class CusotmOAuth2User extends DefaultOAuth2User {
    // Set<GrantedAuthority> authorities, Map<String, Object> attributes, String nameAttributeKey
    // 위 3개 외에 아래 필드를 추가로 가진다
    private final String socialId;
    private final String email; //email은 JWT Token을 발급하기 위한 용도
    private final Role role;

    public CusotmOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes, String nameAttributeKey,
                            String socialId,
                            String email, Role role) {
        super(authorities, attributes, nameAttributeKey);
        this.socialId = socialId;
        this.email = email;
        this.role = role;
    }
}
