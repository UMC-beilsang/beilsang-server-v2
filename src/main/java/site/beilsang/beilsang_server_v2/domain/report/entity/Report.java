package site.beilsang.beilsang_server_v2.domain.report.entity;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.global.common.BaseEntity;
import site.beilsang.beilsang_server_v2.global.enums.ReportStatus;
import site.beilsang.beilsang_server_v2.global.enums.ReportType;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    uniqueConstraints = {
        // 한 회원이 같은 피드를 중복 신고할 수 없음
        @UniqueConstraint(columnNames = {"member_id", "feed_id"}),
        // 한 회원이 같은 챌린지를 중복 신고할 수 없음
        @UniqueConstraint(columnNames = {"member_id", "challenge_id"})
    }
)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feed_id")
    private Feed feed;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    // 신고 사유 코드 (FeedReportReason 또는 ChallengeReportReason의 name())
    @Column(nullable = false)
    private String reason;

    // 기타(ETC) 사유 직접 입력
    @Column(columnDefinition = "TEXT")
    private String detail;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.RECEIVED;

    /**
     * 저장 전 검증: feed와 challenge 중 정확히 하나만 설정되어야 한다.
     */
    @PrePersist
    private void validateTarget() {
        if ((feed == null) == (challenge == null)) {
            throw new IllegalStateException("신고 대상은 피드 또는 챌린지 중 하나만 지정해야 합니다.");
        }
    }
}
