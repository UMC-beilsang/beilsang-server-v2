package site.beilsang.beilsang_server_v2.domain.member.dto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.beilsang.beilsang_server_v2.domain.feed.dto.FeedAssembler;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberLoginResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MyPageResDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.global.enums.Provider;
import site.beilsang.beilsang_server_v2.global.enums.Role;
import site.beilsang.beilsang_server_v2.global.oauth.dto.OAuth2UserInfo;

import java.util.List;

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

    public static MemberLoginResDTO toMemberLoginResDTO(String accessToken, String refreshToken, Role role) {
        return MemberLoginResDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(role)
                .build();
    }
    public static MyPageResDTO toMyPageResDTO(Member member, List<Feed> feedList, Long countFeed, Long countSuccessChallenge, Long countChallenge, Long countFailedChallenge, Long countLike) {
        MyPageResDTO myPageResDTO = MyPageResDTO.builder()
                .feedDTOs(FeedAssembler.toEntities(feedList))
                .countFeed(countFeed)
                .successChallenge(countSuccessChallenge)
                .challenges(countChallenge)
                .failedChallenges(countFailedChallenge)
                .likes(countLike)
                .build();
        myPageResDTO.setMemberInfo(member);
        return myPageResDTO;
    }
}
