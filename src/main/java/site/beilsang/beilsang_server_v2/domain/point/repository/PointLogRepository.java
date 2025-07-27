package site.beilsang.beilsang_server_v2.domain.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;
import site.beilsang.beilsang_server_v2.global.enums.PointStatus;

import java.util.List;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {
    List<PointLog> findAllByMemberId(Long memberId);
    
    List<PointLog> findByMemberIdAndChallengeId(Long memberId, Long challengeId);
    
    List<PointLog> findByMemberIdAndChallengeIdAndStatus(Long memberId, Long challengeId, PointStatus status);
}