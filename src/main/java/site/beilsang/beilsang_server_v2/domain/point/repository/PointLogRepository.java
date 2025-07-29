package site.beilsang.beilsang_server_v2.domain.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;

import java.util.List;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {
    List<PointLog> findAllByMemberId(Long memberId);

    List<PointLog> findByMemberIdAndChallengeId(Long memberId, Long challengeId);
}