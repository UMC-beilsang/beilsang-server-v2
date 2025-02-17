package site.beilsang.beilsang_server_v2.domain.member.dto.res;

import lombok.Builder;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedResDto;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.global.enums.Gender;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class MyPageResDTO {
    //member
    String resolution;
    Integer points;
    String nickName;
    String profileImage;
    String address;
    Gender gender;
    LocalDate birth;
    //feed
    List<PreviewFeedResDto> feedDTOs;
    Long countFeed;
    //challenge
    Long challenges;
    Long failedChallenges;
    Long successChallenge;
    //like
    Long likes;

    public void setMemberInfo(Member member){
        this.resolution = member.getResolution();
        this.gender = member.getGender();
        this.profileImage = member.getProfileUrl();
        this.birth = member.getBirth();
        this.points = member.getPoint();
        this.nickName = member.getNickName();
        this.address = member.getAddress();
    }
}
