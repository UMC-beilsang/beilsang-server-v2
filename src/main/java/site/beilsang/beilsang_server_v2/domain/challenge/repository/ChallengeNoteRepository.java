package site.beilsang.beilsang_server_v2.domain.challenge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.ChallengeNote;

public interface ChallengeNoteRepository extends JpaRepository<ChallengeNote, Long> {

}
