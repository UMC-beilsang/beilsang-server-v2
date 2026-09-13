package site.beilsang.beilsang_server_v2.domain.challenge.entity;

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
import java.util.ArrayList;
import java.util.List;
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

    @Builder.Default
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeInfoImage> infoImages = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeCertImage> certImages = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChallengeNote> challengeNotes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChallengeLike> challengeLikes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Feed> feeds = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChallengeMember> challengeMembers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ChallengePeriod period;

    private Integer totalGoalDay;

    @Builder.Default
    private Integer attendeeCount = 1;

    @Builder.Default
    private Integer countLikes = 0;

    @Builder.Default
    private Integer viewCount = 0;

    private Integer collectedPoint;

    // 포인트 정산 완료 여부 (중복 정산 방지용)
    @Builder.Default
    private Boolean isSettled = false;

    @Builder.Default
    @Column(nullable = false)
    private Integer reportCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isHidden = false;

    public void updateStatus(ChallengeStatus status) {
        this.status = status;
    }

    /**
     * 챌린지 포인트 정산 완료 처리
     */
    public void markAsSettled() {
        this.isSettled = true;
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

    /**
     * 챌린지 찜 수 증가
     */
    public void incrementLikeCount() {
        this.countLikes++;
    }

    /**
     * 챌린지 조회수 증가
     */
    public void incrementViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        this.viewCount++;
    }

    /**
     * 챌린지 찜 수 감소
     */
    public void decrementLikeCount() {
        if (this.countLikes > 0) {
            this.countLikes--;
        }
    }

    public void incrementReportCount() {
        this.reportCount++;
    }

    public void hide() {
        this.isHidden = true;
    }
}
