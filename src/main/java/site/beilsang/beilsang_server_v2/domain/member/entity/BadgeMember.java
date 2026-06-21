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
@Table(
    uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "badge_id"})
)
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
     * 카테고리 배지의 현재 획득 개수
     * 1단계(새싹) : 해당 카테고리 챌린지  0개 성공
     * 2단계(씨앗) : 해당 카테고리 챌린지  1개 성공
     * 3단계(나무) : 해당 카테고리 챌린지  3개 성공
     * 4단계(숲)   : 해당 카테고리 챌린지 10개 성공
     */
    private Integer count;

    // -------------------------------------------------------
    // 비즈니스 메서드
    // -------------------------------------------------------

    /**
     * 카테고리 챌린지 성공 시 카운트 증가
     */
    public void addCount() {
        if (this.count == null) {
            this.count = 0;
        }
        this.count++;
    }

    /**
     * 카테고리 배지 단계 계산 (최대 4단계)
     */
    public int getStep() {
        if (this.count == null) return 1; // 방어 로직 (활동 배지 등의 경우)

        if (this.count >= 10) {
            return 4; // 숲
        } else if (this.count >= 3) {
            return 3; // 나무
        } else if (this.count >= 1) {
            return 2; // 씨앗
        } else {
            return 1; // 새싹
        }
    }

    /**
     * 대표 배지 설정 가능 여부 — 4단계(숲)에 도달한 카테고리 배지만 가능
     */
    public boolean isEligibleForRepresentative() {
        return getStep() == 4;
    }
}
