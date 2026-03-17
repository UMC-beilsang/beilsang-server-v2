package site.beilsang.beilsang_server_v2.domain.member.dto;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.ChallengeCountResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.CheckEnrolledResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.FeedCountResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.LikeCountResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberLoginResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberProfileImageResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberNicknameResDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.global.enums.Provider;
import site.beilsang.beilsang_server_v2.global.enums.Role;
import site.beilsang.beilsang_server_v2.global.oauth.dto.OAuth2UserInfo;

@Slf4j
@Component
public class MemberAssembler {


    public static Member toEntity(Provider provider, OAuth2UserInfo oAuth2UserInfo) {
        return Member.builder()
            .provider(provider)
            .socialId(oAuth2UserInfo.getId())
            .email(oAuth2UserInfo.getEmail())
            .role(Role.GUEST)
            .build();
    }

    public static Member toEntity(Provider provider, OAuth2UserInfo oAuth2UserInfo, String nickName) {
        return Member.builder()
            .provider(provider)
            .socialId(oAuth2UserInfo.getId())
            .email(oAuth2UserInfo.getEmail())
            .nickName(nickName)
            .role(Role.GUEST)
            .build();
    }

    public static MemberLoginResDTO toMemberLoginResDTO(String accessToken, String refreshToken,
        Boolean isTermsAgreed) {
        return MemberLoginResDTO.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .isTermsAgreed(isTermsAgreed)
            .build();
    }

    public static MemberNicknameResDTO toNicknameResDTO(Member member) {
        return MemberNicknameResDTO.builder()
            .nickName(member.getNickName())
            .build();
    }

    public static MemberProfileImageResDTO toProfileImageResDTO(Member member) {
        return MemberProfileImageResDTO.builder()
            .profileUrl(member.getProfileUrl())
            .build();
    }


    public static CheckEnrolledResDTO toCheckEnrolledDTO(Boolean isEnrolled,
        List<Long> enrolledChallengeIds) {
        return CheckEnrolledResDTO.builder()
            .isEnrolled(isEnrolled)
            .enrolledChallengeIds(enrolledChallengeIds)
            .build();
    }

    public static FeedCountResDTO toFeedCountResDTO(Long countFeed) {
        return FeedCountResDTO.builder()
            .countFeed(countFeed)
            .build();
    }

    public static ChallengeCountResDTO toChallengeCountResDTO(Long countChallenge,
        Long countSuccessChallenge, Long countFailedChallenge) {
        return ChallengeCountResDTO.builder()
            .challenges(countChallenge)
            .successChallenge(countSuccessChallenge)
            .failedChallenges(countFailedChallenge)
            .build();
    }

    public static LikeCountResDTO toLikeCountResDTO(Long countLike) {
        return LikeCountResDTO.builder()
            .likes(countLike)
            .build();
    }
}
