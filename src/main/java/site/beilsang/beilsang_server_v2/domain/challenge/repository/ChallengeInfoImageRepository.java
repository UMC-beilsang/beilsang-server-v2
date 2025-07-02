package site.beilsang.beilsang_server_v2.domain.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.ChallengeInfoImage;

@Repository
public interface ChallengeInfoImageRepository extends JpaRepository<ChallengeInfoImage, Long> {
}