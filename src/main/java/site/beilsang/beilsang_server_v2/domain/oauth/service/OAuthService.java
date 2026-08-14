package site.beilsang.beilsang_server_v2.domain.oauth.service;

import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import site.beilsang.beilsang_server_v2.domain.member.dto.MemberAssembler;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberLoginResDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.oauth.dto.req.AppleLoginReqDTO;
import site.beilsang.beilsang_server_v2.domain.oauth.dto.req.KakaoLoginReqDTO;
import site.beilsang.beilsang_server_v2.domain.point.service.PointService;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.config.KakaoTokenConfig;
import site.beilsang.beilsang_server_v2.global.config.PointProperties;
import site.beilsang.beilsang_server_v2.global.enums.PointName;
import site.beilsang.beilsang_server_v2.global.enums.Provider;
import site.beilsang.beilsang_server_v2.global.config.AppleTokenConfig;
import site.beilsang.beilsang_server_v2.global.feign.KakaoClient;
import site.beilsang.beilsang_server_v2.global.jwt.JwtTokenProvider;
import site.beilsang.beilsang_server_v2.global.oauth.dto.AppleTokenRes;
import site.beilsang.beilsang_server_v2.global.oauth.dto.OAuthAttributes;
import site.beilsang.beilsang_server_v2.global.util.NicknameGenerator;

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
    private final NicknameGenerator nicknameGenerator;
    private final PointService pointService;
    private final PointProperties pointProperties;

    private static final String KAKAO_PREFIX = "KakaoAK ";
    private static final String KAKAO_TARGET_TYPE = "user_id";
    private static final String NICKNAME_REGEX = "^[a-zA-Z0-9가-힣]{2,15}$";
    private static final int MAX_NICKNAME_RETRY = 5;

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    @Value("${member.default-profile-image}")
    private String defaultProfileImageUrl;

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

        log.info("OAuthAttributes created for Kakao user: {}", oAuthAttributes.getOAuth2UserInfo().getId());

        // 3. 기존 사용자 확인 또는 새 사용자 생성
        MemberResult result = getOrCreateMember(oAuthAttributes, Provider.KAKAO);
        Member member = result.member();
        boolean isTermsAgreed = result.isTermsAgreed();

        // 4. JWT 토큰 생성
        String accessToken = jwtTokenProvider.createAccessToken(
            member.getSocialId(), member.getEmail()
        );
        String refreshToken = jwtTokenProvider.createRefreshToken(
            member.getSocialId(), member.getEmail()
        );

        // 5. 응답 DTO 생성
        MemberLoginResDTO response = MemberAssembler.toMemberLoginResDTO(
            member.getId(), accessToken, refreshToken, isTermsAgreed
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
        boolean isTermsAgreed = result.isTermsAgreed();

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
        MemberLoginResDTO response = MemberAssembler.toMemberLoginResDTO(member.getId(), accessToken, refreshToken, isTermsAgreed);

        log.info("Apple login successful for user: {}", member.getSocialId());
        return response;
    }

    private MemberResult getOrCreateMember(OAuthAttributes attributes, Provider provider) {
        return memberRepository.findBySocialIdAndProvider(
            attributes.getOAuth2UserInfo().getId(), provider
        ).map(member -> {
            log.info("기존 사용자 로그인: {}", member.getSocialId());
            // 약관동의 여부로 기존 회원 판단
            boolean isTermsAgreed = member.getTermsAgreed() != null && member.getTermsAgreed();
            return new MemberResult(member, isTermsAgreed);
        }).orElseGet(() -> {
            log.info("새 사용자 생성: {}", attributes.getOAuth2UserInfo().getId());
            Member newMember = createMemberWithUniqueNickname(provider, attributes);
            return new MemberResult(newMember, false);
        });
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

                Member newMember = MemberAssembler.toEntity(provider, attributes.getOAuth2UserInfo(), randomNickname, defaultProfileImageUrl);
                Member savedMember = memberRepository.saveAndFlush(newMember);

                // 신규 가입 보상 지급
                int newMemberReward = pointProperties.getNewMemberReward();
                pointService.grantPoints(savedMember, newMemberReward, PointName.NEW_MEMBER);
                log.info("신규 회원 가입 보상 지급 완료: {} 포인트", newMemberReward);

                log.info("회원 생성 성공 - 닉네임: {}", randomNickname);
                return savedMember;

            } catch (DataIntegrityViolationException e) {
                // UNIQUE 제약 위반 (닉네임 중복)만 재시도
                log.warn("닉네임 중복 발생 - 시도 {}/{}: {}", attempt, MAX_NICKNAME_RETRY, e.getMessage());
                if (attempt == MAX_NICKNAME_RETRY) {
                    log.error("닉네임 생성 최대 재시도 횟수 초과 - 중복 해결 실패");
                    throw new BaseException(BaseResponseCode.NICKNAME_GENERATION_FAILED);
                }
                log.info("재시도 {}/{}", attempt + 1, MAX_NICKNAME_RETRY);
            } catch (IllegalStateException e) {
                // NicknameGenerator 설정 오류 (adjectives/nouns 미로드)
                log.error("닉네임 생성기 설정 오류: {}", e.getMessage());
                throw e; // 설정 오류는 재시도 없이 즉시 전파
            } catch (Exception e) {
                // 기타 예상치 못한 오류 (DB 연결 오류 등)
                log.error("회원 생성 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
                throw e; // 치명적 오류는 재시도 없이 즉시 전파
            }
        }

        throw new BaseException(BaseResponseCode.NICKNAME_GENERATION_FAILED);
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

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new BaseException(BaseResponseCode.INVALID_REFRESH_TOKEN);
        }

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
            member.getId(), newAccessToken, newRefreshToken, true
        );
    }

    public void validateNickname(String nickname) {
        // 닉네임 형식 검사 및 중복 검사
        if (nickname == null || nickname.isEmpty()) {
            throw new BaseException(BaseResponseCode.NULL_REQUEST_PARAM);
        } else if (!nickname.matches(NICKNAME_REGEX)) {
            throw new BaseException(INVALID_NICKNAME_FORMAT);
        } else if (memberRepository.existsByNickName(nickname)) {
            throw new BaseException(DUPLICATE_NICKNAME);
        }
    }

    // Member와 약관동의 여부를 함께 반환하는 record 클래스
    private record MemberResult(Member member, boolean isTermsAgreed) {
    }


    @Transactional
    public void updateDeviceToken(Long memberId, String deviceToken) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        // 엔티티의 토큰 갱신 메서드 호출 (더티 체킹으로 자동 UPDATE 쿼리 발생)
        member.updateDeviceToken(deviceToken);
    }
}
