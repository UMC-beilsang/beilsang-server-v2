package site.beilsang.beilsang_server_v2.global.oauth;

import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import site.beilsang.beilsang_server_v2.global.enums.Role;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    // Set<GrantedAuthority> authorities, Map<String, Object> attributes, String nameAttributeKey
    // 위 3개 외에 아래 필드를 추가로 가진다
    private final Long memberId;
    private final String socialId;
    private final String email;
    private final Role role;
    private final Boolean isExistMember;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
        Map<String, Object> attributes, String nameAttributeKey,
        Long memberId,
        String socialId,
        String email, Role role, Boolean isExistMember) {
        super(authorities, attributes, nameAttributeKey);
        this.memberId = memberId;
        this.socialId = socialId;
        this.email = email;
        this.role = role;
        this.isExistMember = isExistMember;
    }
}
