package site.beilsang.beilsang_server_v2.domain.feed.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;

import java.util.List;
import java.util.Optional;

public interface FeedRepository extends JpaRepository<Feed, Long> {
    Long countByChallengeMember_IdIn(List<Long> challengeMemberIds);

    List<Feed> findTop4ByChallengeMember_IdInOrderByCreatedAtDesc(List<Long> challengeMemberIds);

}
