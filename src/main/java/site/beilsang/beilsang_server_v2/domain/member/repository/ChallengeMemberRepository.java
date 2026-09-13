package site.beilsang.beilsang_server_v2.domain.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;

@Repository
public interface ChallengeMemberRepository extends JpaRepository<ChallengeMember, Long> {

    // find
    List<ChallengeMember> findAllByMemberId(Long memberId);

    // count
    Long countByMemberIdAndChallengeMemberStatus(Long memberId,
        ChallengeMemberStatus challengeMemberStatus);

    Long countByMemberId(Long memberId);

    // 챌린지-멤버 단건 조회
    Optional<ChallengeMember> findByChallengeIdAndMemberId(Long challengeId, Long memberId);

    // 챌린지 호스트 조회
    Optional<ChallengeMember> findByChallengeIdAndIsHost(Long challengeId, Boolean isHost);

    // 특정 챌린지의 특정 상태 멤버 목록 조회 (정산 시 ONGOING 멤버 조회용)
    List<ChallengeMember> findAllByChallengeIdAndChallengeMemberStatus(
        Long challengeId, ChallengeMemberStatus challengeMemberStatus);

    // 특정 챌린지의 여러 상태 멤버 목록 조회 (시작 알림용)
    List<ChallengeMember> findAllByChallengeIdAndChallengeMemberStatusIn(
        Long challengeId, List<ChallengeMemberStatus> challengeMemberStatuses);

    // 특정 멤버의 챌린지 참여 상태 배치 조회 (N+1 방지용)
    List<ChallengeMember> findAllByChallengeIdInAndMemberId(List<Long> challengeIds, Long memberId);

    // 오늘 인증하지 않은 진행 중인 챌린지 멤버 조회 (목표 달성자 제외)
    @Query("SELECT cm FROM ChallengeMember cm " +
        "JOIN FETCH cm.challenge c " +
        "JOIN FETCH cm.member m " +
        "WHERE cm.challengeMemberStatus = :status " +
        "AND cm.isFeedUpload = false " +
        "AND cm.successDays < c.totalGoalDay " +
        "AND c.isHidden = false")
    List<ChallengeMember> findOngoingAndUncertifiedMembers(@Param("status") ChallengeMemberStatus status);

    // 자정에 일일 인증 플래그를 초기화
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ChallengeMember cm set cm.isFeedUpload = false " +
        "where cm.challengeMemberStatus = :status and cm.challenge.isHidden = false")
    int resetFeedUploadStatusByChallengeMemberStatus(@Param("status") ChallengeMemberStatus status);
}
