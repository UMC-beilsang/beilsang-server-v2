package site.beilsang.beilsang_server_v2.domain.badge.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.BadgeType;

@Entity
@Table(name = "badge", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"badge_type", "category"})
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "badge_id")
    private Long id;

    /**
     * 배지 종류
     * - CHALLENGE_START   : 챌린지 시작 배지 (챌린지 1개 참여)
     * - CHALLENGE_CREATE  : 챌린지 제작 배지 (챌린지 1개 제작)
     * - CHALLENGE_VERIFY  : 챌린지 인증 배지 (인증 피드 1개 인증)
     * - CATEGORY          : 카테고리 배지    (카테고리별 챌린지 성공 수에 따라 1~3단계)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false)
    private BadgeType badgeType;

    /**
     * 카테고리 배지일 때만 사용. 활동 배지(CHALLENGE_*)는 null.
     */
    @Enumerated(EnumType.STRING)
    private Category category;
}
