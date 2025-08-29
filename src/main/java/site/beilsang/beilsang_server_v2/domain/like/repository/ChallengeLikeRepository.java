package site.beilsang.beilsang_server_v2.domain.like.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.like.entity.ChallengeLike;

public interface ChallengeLikeRepository extends JpaRepository<ChallengeLike, Long> {

    Long countByMemberId(Long memberId);
}
