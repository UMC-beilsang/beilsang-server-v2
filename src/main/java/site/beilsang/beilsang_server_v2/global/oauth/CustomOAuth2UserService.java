package site.beilsang.beilsang_server_v2.global.oauth;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.beilsang.beilsang_server_v2.domain.member.dto.MemberAssembler;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.point.service.PointService;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.config.PointProperties;
import site.beilsang.beilsang_server_v2.global.enums.PointName;
import site.beilsang.beilsang_server_v2.global.enums.Provider;
import site.beilsang.beilsang_server_v2.global.oauth.dto.OAuthAttributes;
import site.beilsang.beilsang_server_v2.global.util.NicknameGenerator;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    // 유저 정보를 가져와 회원 정보가 없다면 저장

    private final MemberRepository memberRepository;
    private final PointService pointService;
    private final PointProperties pointProperties;
    private final NicknameGenerator nicknameGenerator;
    private static final int MAX_NICKNAME_RETRY = 5;

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
        OAuthAttributes oAuthAttributes = OAuthAttributes.of(provider, userNameAttributeName,
            attributes);
        return getMember(attributes, oAuthAttributes, provider);
    }

    /**
     * SocialType과 attributes에 들어있는 소셜 로그인의 식별값 id를 통해 회원을 찾아 반환하는 메소드 만약 찾은 회원이 있다면, 그대로 반환하고 없다면
     * save()를 호출하여 회원을 저장한다.
     */
    private OAuth2User getMember(Map<String, Object> userInfo, OAuthAttributes attributes,
        Provider provider) {
        Optional<Member> memberOpt = memberRepository.findBySocialIdAndProvider(
            attributes.getOAuth2UserInfo().getId(), provider);
        boolean isExistMember = memberOpt.isPresent();
        Member member;
        if (isExistMember) {
            log.info("이미 존재하는 user, findUser return");
            member = memberOpt.get();
        } else {
            log.info("존재하지 않는 유저, 추가하여 return");
            member = createMemberWithUniqueNickname(provider, attributes);

            // 신규 가입 보상 지급
            int newMemberReward = pointProperties.getNewMemberReward();
            pointService.grantPoints(member, newMemberReward, PointName.NEW_MEMBER);
            log.info("신규 회원 가입 보상 지급 완료: {} 포인트", newMemberReward);
        }
        return new CustomOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority(member.getRole().getRole())),
            userInfo,
            attributes.getNameAttributesKey(),
            member.getSocialId(),
            member.getEmail(),
            member.getRole(),
            isExistMember
        );
    }

    /**
     * 중복되지 않는 닉네임으로 회원 생성 (최대 5회 재시도)
     * UNIQUE 제약 위반 시에만 재시도하고, 그 외 예외는 즉시 전파
     */
    private Member createMemberWithUniqueNickname(Provider provider, OAuthAttributes attributes) {
        for (int attempt = 1; attempt <= MAX_NICKNAME_RETRY; attempt++) {
            try {
                String randomNickname = nicknameGenerator.generateRandomNickname();
                log.info("랜덤 닉네임 생성 시도 {}/{}: {}", attempt, MAX_NICKNAME_RETRY, randomNickname);

                Member newMember = MemberAssembler.toEntity(provider, attributes.getOAuth2UserInfo(), randomNickname);
                Member savedMember = memberRepository.saveAndFlush(newMember);

                log.info("회원 생성 성공 - 닉네임: {}", randomNickname);
                return savedMember;

            } catch (DataIntegrityViolationException e) {
                log.warn("닉네임 중복 발생 - 시도 {}/{}", attempt, MAX_NICKNAME_RETRY);
                if (attempt == MAX_NICKNAME_RETRY) {
                    log.error("닉네임 생성 최대 재시도 횟수 초과");
                    throw new BaseException(BaseResponseCode.NICKNAME_GENERATION_FAILED);
                }
            } catch (IllegalStateException e) {
                log.error("닉네임 생성기 설정 오류: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("회원 생성 중 예상치 못한 오류: {}", e.getMessage(), e);
                throw e;
            }
        }
        throw new BaseException(BaseResponseCode.NICKNAME_GENERATION_FAILED);
    }
}
