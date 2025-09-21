package site.beilsang.beilsang_server_v2.domain.feed.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.feed.entity.FeedLike;

public interface FeedLikeRepository extends JpaRepository<FeedLike, Long> {

    /**
     * 특정 피드와 사용자에 대한 좋아요 조회
     * @param feedId 피드 ID
     * @param memberId 사용자 ID
     * @return 좋아요 엔티티 (존재하지 않으면 Optional.empty())
     */
    Optional<FeedLike> findByFeed_IdAndMember_Id(Long feedId, Long memberId);

    /**
     * 특정 피드와 사용자에 대한 좋아요 존재 여부 확인
     * @param feedId 피드 ID
     * @param memberId 사용자 ID
     * @return 존재 여부
     */
    boolean existsByFeed_IdAndMember_Id(Long feedId, Long memberId);

    /**
     * 특정 피드의 총 좋아요 수 조회
     * @param feedId 피드 ID
     * @return 좋아요 수
     */
    long countByFeed_Id(Long feedId);
}