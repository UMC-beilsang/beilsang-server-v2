package site.beilsang.beilsang_server_v2.domain.feed.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.global.enums.Category;

public interface FeedRepository extends JpaRepository<Feed, Long>, FeedRepositoryCustom {

    // isHidden = false 조건 추가 — 숨김 처리된 피드 제외
    Slice<Feed> findAllByChallenge_CategoryAndIsHiddenFalse(Category category, Pageable pageable);

    // isHidden = false 조건 추가 — 숨김 처리된 피드 제외
    Slice<Feed> findAllByChallengeMember_Member_IdAndIsHiddenFalseOrderByCreatedAtDesc(
        Long memberId, Pageable pageable);

    /**
     * 특정 사용자의 피드 중 특정 카테고리에 속한 피드를 최신순으로 조회 (숨김 제외)
     *
     * @param memberId 사용자 ID
     * @param category 챌린지 카테고리
     * @param pageable 페이징 정보
     * @return 조회된 피드 목록 (Slice)
     */
    Slice<Feed> findAllByChallengeMember_Member_IdAndChallenge_CategoryAndIsHiddenFalseOrderByCreatedAtDesc(
        Long memberId, Category category, Pageable pageable);

    @Query("SELECT COUNT(f) FROM Feed f WHERE f.challengeMember.member.id = :memberId")
    Long countByMemberId(@Param("memberId") Long memberId);

    /**
     * 특정 챌린지의 전체 피드를 최신순으로 조회 (숨김 제외)
     *
     * @param challengeId 챌린지 ID
     * @param pageable    페이징 정보
     * @return 조회된 피드 목록 (Slice)
     */
    Slice<Feed> findAllByChallenge_IdAndIsHiddenFalseOrderByCreatedAtDesc(Long challengeId,
        Pageable pageable);

    /**
     * 특정 챌린지에서 특정 멤버가 작성한 피드를 최신순으로 조회 (인증 피드 조회용, 숨김 제외)
     *
     * @param challengeId 챌린지 ID
     * @param memberId    사용자 ID
     * @param pageable    페이징 정보
     * @return 조회된 피드 목록 (Slice)
     */
    Slice<Feed> findAllByChallenge_IdAndChallengeMember_Member_IdAndIsHiddenFalseOrderByCreatedAtDesc(
        Long challengeId, Long memberId, Pageable pageable);

    /**
     * 챌린지 숨김 처리 시 해당 챌린지의 모든 피드를 일괄 숨김 처리
     *
     * @param challengeId 챌린지 ID
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Feed f SET f.isHidden = true WHERE f.challenge.id = :challengeId")
    void hideAllByChallengeId(@Param("challengeId") Long challengeId);
}
