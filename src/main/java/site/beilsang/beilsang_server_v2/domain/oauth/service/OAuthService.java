package site.beilsang.beilsang_server_v2.domain.oauth.service;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.beilsang.beilsang_server_v2.domain.member.dto.MemberAssembler;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberLoginResDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.oauth.dto.req.AppleLoginReqDTO;
import site.beilsang.beilsang_server_v2.domain.oauth.dto.req.KakaoLoginReqDTO;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.config.KakaoTokenConfig;
import site.beilsang.beilsang_server_v2.global.enums.Provider;
import site.beilsang.beilsang_server_v2.global.config.AppleTokenConfig;
import site.beilsang.beilsang_server_v2.global.feign.KakaoClient;
import site.beilsang.beilsang_server_v2.global.jwt.JwtTokenProvider;
import site.beilsang.beilsang_server_v2.global.oauth.dto.AppleTokenRes;
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
    private final KakaoTokenConfig kakaoTokenConfig;
    private final KakaoClient kakaoClient;

    private static final String KAKAO_PREFIX = "KakaoAK ";
    private static final String KAKAO_TARGET_TYPE = "user_id";

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    @Transactional
    public MemberLoginResDTO loginWithKakao(KakaoLoginReqDTO request) {
        log.info("Kakao login request received");

        // 1. 카카오 액세스 토큰으로 사용자 정보 조회
        Map<String, Object> userInfo = kakaoTokenConfig.validateAndExtractUserInfo(
            request.getIdToken()
        );

        // 2. OAuthAttributes 생성
        OAuthAttributes oAuthAttributes = OAuthAttributes.of(
            Provider.KAKAO, "id", userInfo
        );

        System.out.println("OAuthAttributes created for Kakao user: " + oAuthAttributes.getOAuth2UserInfo().getId());

        // 3. 기존 사용자 확인 또는 새 사용자 생성
        MemberResult result = getOrCreateMember(oAuthAttributes, Provider.KAKAO);
        Member member = result.member();
        System.out.println(member.getSocialId());
        boolean isExistMember = result.isExist();

        // 4. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
            member.getSocialId(), member.getEmail()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(
            member.getSocialId(), member.getEmail()
        );

        // 5. 응답 DTO 생성
        MemberLoginResDTO response = MemberAssembler.toMemberLoginResDTO(
            accessToken, refreshToken, isExistMember
        );

        log.info("Kakao login successful for user: {}", member.getSocialId());
        return response;
    }

    @Transactional
    public MemberLoginResDTO loginWithApple(AppleLoginReqDTO request) {

        log.info("Apple login request received with identityToken");

        // identityToken 검증 및 사용자 정보 추출
        Map<String, Object> userInfo = appleTokenConfig.getUserInfoFromToken(request.getIdentityToken());

        // OAuthAttributes 생성
        OAuthAttributes oAuthAttributes = OAuthAttributes.of(Provider.APPLE, "sub", userInfo);

        // 기존 사용자 확인 또는 새 사용자 생성
        MemberResult result = getOrCreateMember(oAuthAttributes, Provider.APPLE);
        Member member = result.member();
        boolean isExistMember = result.isExist();

        // Apple refresh token 발급 및 저장
        if (request.getAuthorizationCode() != null) {
            try {
                AppleTokenRes appleTokenRes = appleTokenConfig.getAppleToken(
                    request.getAuthorizationCode()
                );
                member.updateAppleRefreshToken(appleTokenRes.refreshToken());
                memberRepository.save(member);
                log.info("Apple refresh token saved for user: {}", member.getSocialId());
            } catch (Exception e) {
                log.warn("Failed to save Apple refresh token: {}", e.getMessage());
                // refresh token 저장 실패해도 로그인은 진행
            }
        }

        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(member.getSocialId(), member.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getSocialId(), member.getEmail());

        // 응답 DTO 생성
        MemberLoginResDTO response = MemberAssembler.toMemberLoginResDTO(accessToken, refreshToken, isExistMember);

        log.info("Apple login successful for user: {}", member.getSocialId());
        return response;
    }

    private MemberResult getOrCreateMember(OAuthAttributes attributes, Provider provider) {
        return memberRepository.findBySocialIdAndProvider(
            attributes.getOAuth2UserInfo().getId(), provider
        ).map(member -> {
            log.info("기존 사용자 로그인: {}", member.getSocialId());
            return new MemberResult(member, true);
        }).orElseGet(() -> {
            log.info("새 사용자 생성: {}", attributes.getOAuth2UserInfo().getId());
            Member newMember = MemberAssembler.toEntity(provider, attributes.getOAuth2UserInfo());
            return new MemberResult(memberRepository.save(newMember), false);
        });
    }

    @Transactional
    public void logoutWithKakao(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(NOT_FOUND_MEMBER));
        if (!Provider.KAKAO.equals(member.getProvider())) {
            throw new BaseException(INVALID_PROVIDER);
        }

        try {
            // 리프레시 토큰 초기화
            member.setRefreshToken(null);
            memberRepository.flush();

            // 카카오 로그아웃
            kakaoClient.logoutUser(
                KAKAO_PREFIX + kakaoAdminKey, KAKAO_TARGET_TYPE,
                Long.valueOf(member.getSocialId())
            );
            log.info("Kakao account successfully logged out: {}", member.getSocialId());

        } catch (FeignException e) {
            String error = e.contentUTF8();

            // 카카오싱크가 아닌 유저는 여기로 떨어짐
            if (error.contains("NotRegisteredUserException") ||
                error.contains("code\":-101")) {

                log.warn("Kakao logout skipped: user not linked in Kakao system. socialId={}",
                    member.getSocialId());

                // 카카오 쪽에 연결 안 된 사용자 → 그냥 통과
            } else {
                log.error("Kakao logout failed: {}", error);
                throw new BaseException(KAKAO_LOGOUT_FAILED);
            }
        } catch (Exception e) {
            log.error("Failed to logout Kakao account", e);
            throw new BaseException(KAKAO_LOGOUT_FAILED);
        }
    }

    @Transactional
    public void unlinkWithKakao(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(NOT_FOUND_MEMBER));
        if (!Provider.KAKAO.equals(member.getProvider())) {
            throw new BaseException(INVALID_PROVIDER);
        }
        try {
            // 카카오 연결 해제
            kakaoClient.unlinkUser(
                KAKAO_PREFIX + kakaoAdminKey, KAKAO_TARGET_TYPE,
                Long.valueOf(member.getSocialId())
            );
        } catch (FeignException e) {
            String error = e.contentUTF8();

            // 카카오싱크가 아닌 유저는 여기로 떨어짐
            if (error.contains("NotRegisteredUserException") ||
                error.contains("code\":-101")) {

                log.warn("Kakao unlink skipped: user not linked in Kakao system. socialId={}",
                    member.getSocialId());

                // 카카오 쪽에 연결 안 된 사용자 → 그냥 통과
            } else {
                log.error("Kakao unlink failed: {}", error);
                throw new BaseException(KAKAO_UNLINK_FAILED);
            }

        } catch (Exception e) {
            log.error("Failed to unlink Kakao account", e);
            throw new BaseException(INTERNAL_SERVER_ERROR);
        }

        // 연동 해제 후 회원 탈퇴 처리
        memberRepository.delete(member);
        log.info("Member deleted: {}", member.getSocialId());
    }

    @Transactional
    public void unlinkWithApple(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(NOT_FOUND_MEMBER));
        if (!Provider.APPLE.equals(member.getProvider())) {
            throw new BaseException(INVALID_PROVIDER);
        }
        try {
            // 1. Apple refresh token으로 revoke
            if (member.getAppleRefreshToken() != null) {
                appleTokenConfig.revoke(member.getAppleRefreshToken());
                log.info("Apple account revoked for user: {}", member.getSocialId());
            } else {
                log.warn("No Apple refresh token found for user: {}", member.getSocialId());
            }

            // 2. 회원 삭제
            memberRepository.delete(member);
            log.info("Member deleted: {}", member.getSocialId());

        } catch (Exception e) {
            log.error("Failed to unlink Apple account", e);
            throw new BaseException(APPLE_REVOKE_FAILED);
        }
    }

    public MemberLoginResDTO refreshToken(String refreshToken) {

        // 1. RefreshToken 유효성 검증
        jwtTokenProvider.validateToken(refreshToken);

        // 2. RefreshToken에서 정보 추출
        String socialId = jwtTokenProvider.getClaimFromToken(refreshToken, "socialId");
        String email = jwtTokenProvider.getClaimFromToken(refreshToken, "email");

        // 3. DB에서 회원 조회
        Member member = memberRepository.findBySocialIdAndEmail(socialId, email)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        // 4. DB에 저장된 RefreshToken과 비교
        if (!refreshToken.equals(member.getRefreshToken())) {
            log.warn("RefreshToken mismatch for user: {}", email);
            throw new BaseException(BaseResponseCode.INVALID_REFRESH_TOKEN);
        }

        // 5. 새로운 AccessToken과 RefreshToken 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(socialId, email);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(socialId, email);

        log.info("Tokens reissued for user: {}", email);
        return MemberAssembler.toMemberLoginResDTO(
            newAccessToken, newRefreshToken, true
        );
    }

    // Member와 isExisting을 함께 반환하는 record 클래스
    private record MemberResult(Member member, boolean isExist) {
    }
}
