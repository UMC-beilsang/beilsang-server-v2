package site.beilsang.beilsang_server_v2.domain.point.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;
import site.beilsang.beilsang_server_v2.global.enums.PointStatus;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {

    List<PointLog> findAllByMemberId(Long memberId);

    List<PointLog> findByMemberIdAndChallengeId(Long memberId, Long challengeId);

    List<PointLog> findAllByMemberIdAndStatusNot(Long memberId, PointStatus status);
}
