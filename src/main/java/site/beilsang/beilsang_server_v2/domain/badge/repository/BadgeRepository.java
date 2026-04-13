package site.beilsang.beilsang_server_v2.domain.badge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.badge.entity.Badge;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeBadgeType;

import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    Optional<Badge> findByBadgeType(ChallengeBadgeType badgeType);
}
