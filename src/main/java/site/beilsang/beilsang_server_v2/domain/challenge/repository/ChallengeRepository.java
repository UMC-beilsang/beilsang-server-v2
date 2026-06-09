package site.beilsang.beilsang_server_v2.domain.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.global.enums.Category;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long>,
    ChallengeRepositoryCustom {

    // 숨김 처리된 챌린지 제외
    Challenge getChallengeByIdAndIsHiddenFalse(Long id);

    // 숨김 처리된 챌린지 제외
    List<Challenge> findTop10ByIsHiddenFalseOrderByCountLikesDescStartDateDesc();

    // 숨김 처리된 챌린지 제외
    List<Challenge> findTop10ByCategoryAndIsHiddenFalseOrderByCountLikesDescStartDateDesc(Category category);
}
