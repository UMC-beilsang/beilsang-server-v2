package site.beilsang.beilsang_server_v2.domain.oauth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.beilsang.beilsang_server_v2.domain.member.dto.MemberAssembler;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberLoginResDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.oauth.dto.req.AppleLoginReqDto;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;
import site.beilsang.beilsang_server_v2.global.enums.Provider;
import site.beilsang.beilsang_server_v2.global.config.AppleTokenConfig;
import site.beilsang.beilsang_server_v2.global.jwt.JwtTokenProvider;
import site.beilsang.beilsang_server_v2.global.oauth.dto.OAuthAttributes;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final MemberRepository memberRepository;
    private final AppleTokenConfig appleTokenConfig;
    private final JwtTokenProvider jwtTokenProvider;

    public BaseResponse<MemberLoginResDTO> loginWithApple(AppleLoginReqDto request) {

        log.info("Apple login request received with identityToken");

        // identityToken 검증 및 사용자 정보 추출
        Map<String, Object> userInfo = appleTokenConfig.getUserInfoFromToken(request.getIdentityToken());

        // OAuthAttributes 생성
        OAuthAttributes oAuthAttributes = OAuthAttributes.of(Provider.APPLE, "sub", userInfo);

        // 기존 사용자 확인 또는 새 사용자 생성
        MemberResult result = getOrCreateMember(oAuthAttributes);
        Member member = result.member();
        boolean isExistMember = result.isExist();

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(member.getSocialId(), member.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getSocialId(), member.getEmail());

        // 응답 DTO 생성
        MemberLoginResDTO response = MemberAssembler.toMemberLoginResDTO(accessToken, refreshToken, isExistMember);

        log.info("Apple login successful for user: {}", member.getSocialId());
        return new BaseResponse<>(response);
    }

    private MemberResult getOrCreateMember(OAuthAttributes attributes) {
        return memberRepository.findBySocialIdAndProvider(
            attributes.getOAuth2UserInfo().getId(), Provider.APPLE
        ).map(member -> {
            log.info("기존 사용자 로그인: {}", member.getSocialId());
            return new MemberResult(member, true);
        }).orElseGet(() -> {
            log.info("새 사용자 생성: {}", attributes.getOAuth2UserInfo().getId());
            Member newMember = MemberAssembler.toEntity(Provider.APPLE, attributes.getOAuth2UserInfo());
            return new MemberResult(memberRepository.save(newMember), false);
        });
    }

    // Member와 isExisting을 함께 반환하는 record 클래스
    private record MemberResult(Member member, boolean isExist) {}
}
