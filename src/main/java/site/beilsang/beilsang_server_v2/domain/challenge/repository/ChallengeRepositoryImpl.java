package site.beilsang.beilsang_server_v2.domain.challenge.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListRequestDTO;
import site.beilsang.beilsang_server_v2.global.enums.SortDirection;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.QChallenge;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class ChallengeRepositoryImpl implements ChallengeRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Challenge> findChallenges(ChallengeListRequestDTO requestDTO, Pageable pageable) {
        QChallenge challenge = QChallenge.challenge;
        BooleanBuilder builder = new BooleanBuilder();
        LocalDate today = LocalDate.now();

        // 카테고리 필터링
        if (requestDTO.getCategory() != null) {
            builder.and(challenge.category.eq(requestDTO.getCategory()));
        }
        // isFinished 필터링
        if (requestDTO.getIsFinished() != null) {
            if (requestDTO.getIsFinished()) {
                builder.and(challenge.finishDate.lt(today));
            } else {
                builder.and(challenge.finishDate.goe(today));
            }
        }
        // isJoined 필터링
        if (requestDTO.getIsJoined() != null && requestDTO.getMemberId() != null) {
            if (requestDTO.getIsJoined()) {
                builder.and(challenge.challengeMembers.any().member.id.eq(requestDTO.getMemberId()));
            } else {
                builder.and(challenge.challengeMembers.any().member.id.ne(requestDTO.getMemberId()));
            }
        }

        // 기본 쿼리
        var query = queryFactory.selectFrom(challenge)
                .where(builder);

        // 정렬 처리
        if (requestDTO.getSortField() != null && requestDTO.getSortDirection() != null) {
            boolean asc = SortDirection.ASC == requestDTO
                    .getSortDirection();
            switch (requestDTO.getSortField()) {
                case ATTENDEE_COUNT -> query.orderBy(asc ? challenge.attendeeCount.asc() : challenge.attendeeCount.desc());
                case COUNT_LIKES -> query.orderBy(asc ? challenge.countLikes.asc() : challenge.countLikes.desc());
                case START_DATE -> query.orderBy(asc ? challenge.startDate.asc() : challenge.startDate.desc());
                case FINISH_DATE -> query.orderBy(asc ? challenge.finishDate.asc() : challenge.finishDate.desc());
            }
        }

        // 페이징
        List<Challenge> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 쿼리
        long total = queryFactory
                .select(Wildcard.count)
                .from(challenge)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}