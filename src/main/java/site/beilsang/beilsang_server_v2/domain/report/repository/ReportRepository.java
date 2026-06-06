package site.beilsang.beilsang_server_v2.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.report.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * 특정 회원이 특정 피드를 이미 신고했는지 확인 (중복 신고 방지)
     *
     * @param memberId 회원 ID
     * @param feedId   피드 ID
     * @return 신고 여부
     */
    boolean existsByMemberIdAndFeedId(Long memberId, Long feedId);

    /**
     * 특정 회원이 특정 챌린지를 이미 신고했는지 확인 (중복 신고 방지)
     *
     * @param memberId    회원 ID
     * @param challengeId 챌린지 ID
     * @return 신고 여부
     */
    boolean existsByMemberIdAndChallengeId(Long memberId, Long challengeId);
}
