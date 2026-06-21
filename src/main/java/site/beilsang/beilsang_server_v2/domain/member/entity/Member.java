package site.beilsang.beilsang_server_v2.domain.member.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import site.beilsang.beilsang_server_v2.domain.feed.entity.FeedLike;
import site.beilsang.beilsang_server_v2.domain.like.entity.ChallengeLike;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;
import site.beilsang.beilsang_server_v2.global.enums.Category;
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
    private Provider provider; // KAKAO, APPLE

    private String socialId;

    private String appleRefreshToken;

    @Column(unique = true)
    private String nickName;

    @Enumerated(EnumType.STRING)
    private Category keyword;

    //알게된 경로
    private String discoveredPath;

    private int point;

    private String recommendNickname;

    private String profileUrl;

    @Setter
    private String refreshToken;

    private String deviceToken;

    @Builder.Default
    private Boolean termsAgreed = false;

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<PointLog> pointLogs = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChallengeMember> challengeMembers = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<FeedLike> feedLikes = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChallengeLike> challengeLikes = new ArrayList<>();


    /** 멤버가 보유한 모든 배지 목록 */
    @Builder.Default
    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<BadgeMember> memberBadges = new ArrayList<>();

    // 대표 배지
    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "representative_badge_id")
    private BadgeMember representativeBadge;

// ── 비즈니스 메서드 추가 ─────────────────────────────────────────────────

    /**
     * 대표 배지 설정 — 4단계 배지만 허용, null로 해제도 가능
     */
    public void updateRepresentativeBadge(BadgeMember memberBadge) {
        if (memberBadge != null && !memberBadge.isEligibleForRepresentative()) {
            throw new IllegalArgumentException("4단계(숲) 배지만 대표 배지로 설정할 수 있습니다.");
        }
        this.representativeBadge = memberBadge;
    }

    /**
     * Member 삭제 시(memberBadges cascade remove) FK 순환으로 인한 삭제 실패를 방지합니다.
     */
    @PreRemove
    private void preRemove() {
        this.representativeBadge = null;
    }


    public void updateNickname(String nickName) {
        this.nickName = nickName;
    }

    public void updateProfileImageUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public void subPoint(int point) {
        this.point -= point;
    }

    public void addPoint(int point) {
        this.point += point;
    }

    public void updateAppleRefreshToken(String refreshToken) {
        this.appleRefreshToken = refreshToken;
    }

    public void agreeToTerms() {
        this.termsAgreed = true;
    }
}
