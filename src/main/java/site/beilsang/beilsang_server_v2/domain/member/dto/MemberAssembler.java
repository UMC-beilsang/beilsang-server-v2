package site.beilsang.beilsang_server_v2.domain.member.dto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.global.enums.Provider;
import site.beilsang.beilsang_server_v2.global.enums.Role;
import site.beilsang.beilsang_server_v2.global.oauth.dto.OAuth2UserInfo;

@Slf4j
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

    public static MemberLoginResDTO toMemberLoginResDTO(String accessToken, String refreshToken, Role role) {
        return MemberLoginResDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(role)
                .build();
    }
}
