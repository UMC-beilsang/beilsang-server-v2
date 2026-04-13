package site.beilsang.beilsang_server_v2.domain.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.beilsang.beilsang_server_v2.domain.badge.entity.Badge;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_badge_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    /**
     * 배지 최초 획득 시각
     */
    @Column(nullable = false)
    private LocalDateTime acquiredAt;

    /**
     * 카테고리 배지의 현재 단계 (1~4단계 / 활동 배지는 null)
     * 1단계(새싹) : 해당 카테고리 챌린지  1개 성공
     * 2단계(씨앗) : 해당 카테고리 챌린지  3개 성공
     * 3단계(나무) : 해당 카테고리 챌린지 10개 성공
     * 4단계(숲)   : 해당 카테고리 챌린지 ??개 성공 (조건 확정 후 서비스 레이어에 반영)
     */
    private Integer currentStep;

    // -------------------------------------------------------
    // 비즈니스 메서드
    // -------------------------------------------------------

    /**
     * 카테고리 배지 단계 업그레이드 (최대 4단계)
     */
    public void upgradeStep() {
        if (this.currentStep != null && this.currentStep < 4) {
            this.currentStep++;
        }
    }

    /**
     * 대표 배지 설정 가능 여부 — 4단계(숲)에 도달한 카테고리 배지만 가능
     */
    public boolean isEligibleForRepresentative() {
        return this.currentStep != null && this.currentStep == 4;
    }
}
