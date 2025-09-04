package site.beilsang.beilsang_server_v2.domain.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.global.enums.Category;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long>,
    ChallengeRepositoryCustom {

    Challenge getChallengeById(Long id);

    List<Challenge> findTop10ByOrderByCountLikesDescStartDateDesc();

    List<Challenge> findTop10ByCategoryOrderByCountLikesDescStartDateDesc(Category category);
}
