package site.beilsang.beilsang_server_v2.domain.member.dto.res;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedResDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;

@Getter
@Builder
public class MyPageResDTO {

    //member
    String resolution;
    Integer points;
    String nickName;
    String profileImage;
    //feed
    List<PreviewFeedResDTO> feedDTOs;
    Long countFeed;
    //challenge
    Long challenges;
    Long failedChallenges;
    Long successChallenge;
    //like
    Long likes;

    public void setMemberInfo(Member member) {
        this.profileImage = member.getProfileUrl();
        this.points = member.getPoint();
        this.nickName = member.getNickName();
    }
}
