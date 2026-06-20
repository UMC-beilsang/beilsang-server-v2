package site.beilsang.beilsang_server_v2.domain.badge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.beilsang.beilsang_server_v2.domain.badge.entity.Badge;
import site.beilsang.beilsang_server_v2.global.enums.BadgeType;
import site.beilsang.beilsang_server_v2.global.enums.Category;

import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {
    @Query("SELECT b FROM Badge b WHERE b.badgeType = :badgeType")
    Optional<Badge> findByBadgeType(@Param("badgeType") BadgeType badgeType);

    @Query("SELECT COUNT(b) > 0 FROM Badge b WHERE b.badgeType = :badgeType")
    boolean existsByBadgeType(@Param("badgeType") BadgeType badgeType);

    Optional<Badge> findByCategory(Category category);

    Boolean existsByCategory(Category category);
}
