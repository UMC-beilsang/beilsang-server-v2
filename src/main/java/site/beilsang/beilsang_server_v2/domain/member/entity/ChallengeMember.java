package site.beilsang.beilsang_server_v2.domain.member.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.global.common.BaseEntity;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_member_id")
    private Long id;

    private Boolean isHost;

    private Integer successDays;

    @Enumerated(EnumType.STRING)
    private ChallengeMemberStatus challengeMemberStatus;

    private Boolean isFeedUpload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @Builder.Default
    @OneToMany(mappedBy = "challengeMember", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Feed> feed = new ArrayList<>();

    /**
     * 챌린지 멤버 상태 변경 (정산 시 ONGOING → SUCCESS/FAIL 전환용)
     */
    public void updateChallengeMemberStatus(ChallengeMemberStatus status) {
        this.challengeMemberStatus = status;
    }

    /**
     * 오늘 피드 인증을 완료했음을 반영합니다.
     * 같은 날 중복 업로드가 들어와도 successDays는 1회만 증가합니다.
     */
    public void markFeedUploaded() {
        if (!Boolean.TRUE.equals(this.isFeedUpload)) {
            this.successDays = this.successDays == null ? 1 : this.successDays + 1;
        }
        this.isFeedUpload = true;
    }

    /**
     * 일일 인증 플래그를 초기화합니다. (자정 리셋용)
     */
    public void resetFeedUpload() {
        this.isFeedUpload = false;
    }
}
