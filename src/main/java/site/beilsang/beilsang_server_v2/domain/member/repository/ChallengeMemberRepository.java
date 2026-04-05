package site.beilsang.beilsang_server_v2.domain.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
