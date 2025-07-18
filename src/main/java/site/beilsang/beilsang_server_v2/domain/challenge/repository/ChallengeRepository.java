package site.beilsang.beilsang_server_v2.domain.challenge.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;

public interface ChallengeRepository extends JpaRepository<Challenge, Long>, ChallengeRepositoryCustom {
    Optional<Challenge> getChallengeById(Long id);
}
