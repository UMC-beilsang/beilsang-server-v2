package site.beilsang.beilsang_server_v2.domain.feed.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.global.enums.Category;

public interface FeedRepository extends JpaRepository<Feed, Long> {

    Long countByChallengeMember_IdIn(List<Long> challengeMemberIds);

    List<Feed> findTop4ByChallengeMember_IdInOrderByCreatedAtDesc(List<Long> challengeMemberIds);

    Page<Feed> findAllByChallenge_Category(Category category, Pageable pageable);
}
