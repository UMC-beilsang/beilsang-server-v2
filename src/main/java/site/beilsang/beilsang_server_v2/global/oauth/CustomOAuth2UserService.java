package site.beilsang.beilsang_server_v2.global.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import site.beilsang.beilsang_server_v2.domain.member.dto.MemberAssembler;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.global.enums.Provider;
import site.beilsang.beilsang_server_v2.global.oauth.dto.OAuthAttributes;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    // 유저 정보를 가져와 회원 정보가 없다면 저장

    private final MemberRepository memberRepository;
    private final MemberAssembler memberAssembler;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("CustomOAuth2UserService.loadUser() 실행 - 로그인 완료, OAuth2 사용자 정보 단계");

        // 로그인 유저 정보 가져옴
        OAuth2UserService<OAuth2UserRequest, OAuth2User> service = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = service.loadUser(userRequest);

        // OAuth2 서비스 id (google, kakao, naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Provider provider = Provider.getByName(registrationId);

        String userNameAttributeName = userRequest.getClientRegistration() // OAuth2 로그인 시 키(PK)가 되는 값
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes(); // 소셜 로그인에서 API가 제공하는 userInfo의 Json 값(유저 정보들)

        // 소셜 종류에 따라 유저 정보를 통해 OAuthAttributes 객체 생성
        OAuthAttributes oAuthAttributes = OAuthAttributes.of(provider, userNameAttributeName, attributes);
        Member member = getMember(oAuthAttributes, provider);

        return new CustomOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole().getRole())),
                attributes,
                oAuthAttributes.getNameAttributesKey(),
                member.getSocialId(),
                member.getEmail(),
                member.getRole()
        );
    }

    /**
     * SocialType과 attributes에 들어있는 소셜 로그인의 식별값 id를 통해 회원을 찾아 반환하는 메소드
     * 만약 찾은 회원이 있다면, 그대로 반환하고 없다면 save()를 호출하여 회원을 저장한다.
     */
    private Member getMember(OAuthAttributes attributes, Provider provider) {
        Optional<Member> member = memberRepository.findBySocialIdAndProvider(
                attributes.getOAuth2UserInfo().getId(), provider);

        if (member.isEmpty()) {
            log.info("존재하지 않는 유저, 추가하여 return");
            Member newMember = memberAssembler.toEntity(provider, attributes.getOAuth2UserInfo());
            return memberRepository.save(newMember);
        } else {
            log.info("이미 존재하는 user, findUser return");
            return member.get();
        }
    }
}
