package site.beilsang.beilsang_server_v2.domain.feed.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.global.enums.Category;

public interface FeedRepository extends JpaRepository<Feed, Long>, FeedRepositoryCustom {

    Long countByChallengeMember_IdIn(List<Long> challengeMemberIds);

    List<Feed> findTop4ByChallengeMember_IdInOrderByCreatedAtDesc(List<Long> challengeMemberIds);

    Slice<Feed> findAllByChallenge_Category(Category category, Pageable pageable);

    Slice<Feed> findAllByChallengeMember_Member_IdOrderByCreatedAtDesc(Long memberId,
                                                                       Pageable pageable);

    /**
     * 특정 사용자의 피드 중 특정 카테고리에 속한 피드를 최신순으로 조회
     *
     * @param memberId 사용자 ID
     * @param category 챌린지 카테고리
     * @param pageable 페이징 정보
     * @return 조회된 피드 목록 (Slice)
     */
    Slice<Feed> findAllByChallengeMember_Member_IdAndChallenge_CategoryOrderByCreatedAtDesc(
        Long memberId, Category category, Pageable pageable);

    @Query("SELECT COUNT(f) FROM Feed f WHERE f.challengeMember.member.id = :memberId")
    Long countByMemberId(@Param("memberId") Long memberId);
}
