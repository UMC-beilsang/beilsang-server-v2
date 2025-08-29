package site.beilsang.beilsang_server_v2.domain.member.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.beilsang.beilsang_server_v2.domain.feed.entity.FeedLike;
import site.beilsang.beilsang_server_v2.domain.like.entity.ChallengeLike;
import site.beilsang.beilsang_server_v2.domain.member.dto.req.MemberProfileReqDTO;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.Gender;
import site.beilsang.beilsang_server_v2.global.enums.Provider;
import site.beilsang.beilsang_server_v2.global.enums.Role;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    private Long id;

    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    private Provider provider; // KAKAO, APPLE

    private String socialId;

    private String nickName;

    private LocalDate birth;

    private String address;

    @Enumerated(EnumType.STRING)
    private Category keyword;

    //알게된 경로
    private String discoveredPath;

    //다짐
    private String resolution;

    private int point;

    private String recommendNickname;

    private String profileUrl;

    @Setter
    private String refreshToken;

    private String deviceToken;

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PointLog> pointLogs = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChallengeMember> challengeMembers = new ArrayList<>();


    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<FeedLike> feedLikes = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChallengeLike> challengeLikes = new ArrayList<>();

    public void updateProfile(MemberProfileReqDTO memberProfileReqDTO) {
        if (!memberProfileReqDTO.getNickName().isBlank()) {
            this.nickName = memberProfileReqDTO.getNickName();
        }
        if (!memberProfileReqDTO.getBirth().isBlank()) {
            this.birth = LocalDate.parse(memberProfileReqDTO.getBirth(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        if (!memberProfileReqDTO.getGender().isBlank()) {
            this.gender = Gender.valueOf(memberProfileReqDTO.getGender());
        }
        if (!memberProfileReqDTO.getAddress().isBlank()) {
            this.address = memberProfileReqDTO.getAddress();
        }
        if (!memberProfileReqDTO.getResolution().isBlank()) {
            this.resolution = memberProfileReqDTO.getResolution();
        }
    }

    public void updateProfileImageUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public void subPoint(int point) {
        this.point -= point;
    }
}
