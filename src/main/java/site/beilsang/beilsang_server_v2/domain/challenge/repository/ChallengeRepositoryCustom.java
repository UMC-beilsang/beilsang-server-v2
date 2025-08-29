package site.beilsang.beilsang_server_v2.domain.challenge.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;

public interface ChallengeRepositoryCustom {

    Page<Challenge> findChallenges(ChallengeListReqDTO requestDTO, Pageable pageable);
}
