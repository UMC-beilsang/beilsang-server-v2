package site.beilsang.beilsang_server_v2.domain.like.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.like.entity.ChallengeLike;

public interface ChallengeLikeRepository extends JpaRepository<ChallengeLike, Long> {

    /**
     * 회원 ID로 찜한 챌린지 수 조회
     * @param memberId 회원 ID
     * @return 찜한 챌린지 수
     */
    Long countByMemberId(Long memberId);

    /**
     * 회원이 특정 챌린지를 찜했는지 확인
     * @param memberId 회원 ID
     * @param challengeId 챌린지 ID
     * @return 찜 여부
     */
    boolean existsByMemberIdAndChallengeId(Long memberId, Long challengeId);

    /**
     * 회원과 챌린지로 찜 엔티티 조회
     * @param memberId 회원 ID
     * @param challengeId 챌린지 ID
     * @return 찜 엔티티 Optional
     */
    Optional<ChallengeLike> findByMemberIdAndChallengeId(Long memberId, Long challengeId);
}
