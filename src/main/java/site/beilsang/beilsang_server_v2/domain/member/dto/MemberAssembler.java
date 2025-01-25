package site.beilsang.beilsang_server_v2.domain.member.dto;

import org.springframework.stereotype.Component;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.global.enums.Provider;
import site.beilsang.beilsang_server_v2.global.enums.Role;
import site.beilsang.beilsang_server_v2.global.oauth.dto.OAuth2UserInfo;

@Component
public class MemberAssembler {

    public Member toEntity(Provider provider, OAuth2UserInfo oAuth2UserInfo) {
        return Member.builder()
                .provider(provider)
                .socialId(oAuth2UserInfo.getId())
                .email(oAuth2UserInfo.getEmail())
                .role(Role.GUEST)
                .build();
    }
}
