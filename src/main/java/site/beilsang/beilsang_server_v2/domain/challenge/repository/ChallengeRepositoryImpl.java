package site.beilsang.beilsang_server_v2.domain.challenge.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListRequestDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;

@RequiredArgsConstructor
public class ChallengeRepositoryImpl implements ChallengeRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Challenge> findChallenges(ChallengeListRequestDTO requestDTO, Pageable pageable) {
        // TODO: QueryDSL 동적 쿼리 구현 예정
        return null;
    }
}