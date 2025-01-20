package site.beilsang.beilsang_server_v2.domain.challenge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.like.entity.ChallengeLike;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.global.common.BaseEntity;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengePeriod;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Challenge extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String title;

    private LocalDate startDate;

    private LocalDate finishDate;

    private Integer joinPoint;

    private String mainImageUrl;

    private String certImageUrl;

    private String details;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChallengeNote> challengeNotes = new ArrayList<>();

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChallengeLike> challengeLikes = new ArrayList<>();

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Feed> feeds = new ArrayList<>();

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChallengeMember> challengeMembers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ChallengePeriod period;

    private Integer totalGoalDay;

    private Integer attendeeCount;

    private Integer countLikes;

    private Integer collectedPoint;
}
