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
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;

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

    @Enumerated(EnumType.STRING)
    private ChallengeStatus status;

    private String title;

    private LocalDate startDate;

    private LocalDate finishDate;

    private Integer joinPoint;

    private String details;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeInfoImage> infoImages = new ArrayList<>();

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeCertImage> certImages = new ArrayList<>();

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

    private Integer attendeeCount = 1;

    private Integer countLikes = 0;

    private Integer collectedPoint;

    public void updateStatus(ChallengeStatus status) {
        this.status = status;
    }

    public ChallengeStatus calculateCurrentStatus(LocalDate today) {
        if (today.isBefore(startDate)) {
            return ChallengeStatus.NOT_YET;
        } else if (today.isAfter(finishDate)) {
            return ChallengeStatus.END;
        } else {
            return ChallengeStatus.IN_PROGRESS;
        }
    }

    /**
     * 챌린지 참여자 수 증가
     */
    public void incrementAttendeeCount() {
        this.attendeeCount++;
    }
}
