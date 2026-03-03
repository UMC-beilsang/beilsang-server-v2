package site.beilsang.beilsang_server_v2.domain.member.dto;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.beilsang.beilsang_server_v2.domain.feed.dto.FeedAssembler;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.CheckEnrolledResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberLoginResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberProfileResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MyPageResDTO;
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

    public static MyPageResDTO toMyPageResDTO(Member member, List<Feed> feedList, Long countFeed,
        Long countSuccessChallenge, Long countChallenge, Long countFailedChallenge,
        Long countLike) {
        MyPageResDTO myPageResDTO = MyPageResDTO.builder()
            .feedDTOs(FeedAssembler.toPreviewFeedResDTOList(feedList))
            .countFeed(countFeed)
            .successChallenge(countSuccessChallenge)
            .challenges(countChallenge)
            .failedChallenges(countFailedChallenge)
            .likes(countLike)
            .build();
        myPageResDTO.setMemberInfo(member);
        return myPageResDTO;
    }

    public static MemberProfileResDTO toProfileResDTO(Member member) {
        return MemberProfileResDTO.builder()
            .nickName(member.getNickName())
            .build();
    }


    public static CheckEnrolledResDTO toCheckEnrolledDTO(Boolean isEnrolled,
        List<Long> enrolledChallengeIds) {
        return CheckEnrolledResDTO.builder()
            .isEnrolled(isEnrolled)
            .enrolledChallengeIds(enrolledChallengeIds)
            .build();
    }
}
