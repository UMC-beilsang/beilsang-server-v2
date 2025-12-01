package site.beilsang.beilsang_server_v2.domain.oauth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.beilsang.beilsang_server_v2.domain.member.dto.MemberAssembler;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberLoginResDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.oauth.dto.req.AppleLoginReqDto;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.enums.Provider;
import site.beilsang.beilsang_server_v2.global.config.AppleTokenConfig;
import site.beilsang.beilsang_server_v2.global.feign.KakaoUnlinkClient;
import site.beilsang.beilsang_server_v2.global.jwt.JwtTokenProvider;
import site.beilsang.beilsang_server_v2.global.oauth.dto.OAuthAttributes;

import java.util.Map;

import static site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {

    private final MemberRepository memberRepository;
    private final AppleTokenConfig appleTokenConfig;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoUnlinkClient kakaoUnlinkClient;

    private static final String KAKAO_PREFIX = "KakaoAK ";
    private static final String KAKAO_TARGET_TYPE = "user_id";

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    @Transactional
    public MemberLoginResDTO loginWithApple(AppleLoginReqDto request) {

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
        return response;
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

    @Transactional
    public void logoutWithKakao(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(NOT_FOUND_MEMBER));
        try {
            // 카카오 로그아웃
            kakaoUnlinkClient.logoutUser(
                KAKAO_PREFIX + kakaoAdminKey, KAKAO_TARGET_TYPE,
                Long.valueOf(member.getSocialId())
            );

            // 리프레시 토큰 초기화
            member.setRefreshToken(null);
            memberRepository.save(member);
            log.info("Kakao account successfully logged out: {}", member.getSocialId());

        } catch (Exception e) {
            log.error("Failed to logout Kakao account", e);
            throw new BaseException(KAKAO_LOGOUT_FAILED);
        }
    }

    @Transactional
    public void unlinkWithKakao(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(NOT_FOUND_MEMBER));
        try {
            // 카카오 연결 해제
            kakaoUnlinkClient.unlinkUser(
                KAKAO_PREFIX + kakaoAdminKey, KAKAO_TARGET_TYPE,
                Long.valueOf(member.getSocialId())
            );

            // 회원 삭제
            memberRepository.delete(member);
            log.info("Member successfully deleted: {}", member.getSocialId());

        } catch (Exception e) {
            log.error("Failed to unlink Kakao account", e);
            throw new BaseException(KAKAO_UNLINK_FAILED);
        }

        // 연동 해제 후 회원 탈퇴 처리
        memberRepository.delete(member);
        log.info("Member deleted: {}", member.getSocialId());
    }

    // Member와 isExisting을 함께 반환하는 record 클래스
    private record MemberResult(Member member, boolean isExist) {}
}
