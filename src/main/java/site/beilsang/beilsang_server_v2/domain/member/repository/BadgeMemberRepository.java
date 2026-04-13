package site.beilsang.beilsang_server_v2.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.beilsang.beilsang_server_v2.domain.member.entity.BadgeMember;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeBadgeType;

import java.util.List;
import java.util.Optional;

public interface BadgeMemberRepository extends JpaRepository<BadgeMember, Long> {
    /**
     * 멤버의 전체 보유 배지 목록 조회
     * Badge 정보를 fetch join으로 함께 로딩 (N+1 방지)
     */
    @Query("SELECT mb FROM BadgeMember mb JOIN FETCH mb.badge WHERE mb.member.id = :memberId")
    List<BadgeMember> findAllByMemberIdWithBadge(@Param("memberId") Long memberId);

    /**
     * 특정 MemberBadge 단건 조회 (본인 소유 여부 확인 포함)
     */
    @Query("SELECT mb FROM BadgeMember mb JOIN FETCH mb.badge WHERE mb.id = :memberBadgeId AND mb.member.id = :memberId")
    Optional<BadgeMember> findByIdAndMemberId(
        @Param("memberBadgeId") Long memberBadgeId,
        @Param("memberId") Long memberId
    );

    Boolean existsByMemberAndChallengeBadgeType(Member member, ChallengeBadgeType badgeType);
}
