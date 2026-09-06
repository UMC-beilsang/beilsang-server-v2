package site.beilsang.beilsang_server_v2.domain.notification.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.beilsang.beilsang_server_v2.domain.notification.entity.AppNotification;

@Repository
public interface NotificationRepository extends JpaRepository<AppNotification, Long> {

    @Query("select case when count(c) > 0 then true else false end from ChallengeNotification c "
        + "where c.challengeId = :challengeId and c.createdAt > :after")
    boolean existsByChallengeIdAndCreatedAtAfter(@Param("challengeId") Long challengeId,
        @Param("after") LocalDateTime after);

    @Query("select case when count(c) > 0 then true else false end from ChallengeNotification c "
        + "where c.challengeId = :challengeId and c.member.id = :memberId and c.createdAt > :after")
    boolean existsByChallengeIdAndMemberIdAndCreatedAtAfter(
        @Param("challengeId") Long challengeId,
        @Param("memberId") Long memberId,
        @Param("after") LocalDateTime after
    );

    Page<AppNotification> findAllByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}


