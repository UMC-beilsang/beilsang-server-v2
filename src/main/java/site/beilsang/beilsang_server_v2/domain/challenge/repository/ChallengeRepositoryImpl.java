package site.beilsang.beilsang_server_v2.domain.challenge.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.QChallenge;
import site.beilsang.beilsang_server_v2.global.enums.SortDirection;

@RequiredArgsConstructor
public class ChallengeRepositoryImpl implements ChallengeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Challenge> findChallenges(ChallengeListReqDTO requestDTO, Pageable pageable) {
        QChallenge challenge = QChallenge.challenge;
        BooleanBuilder builder = new BooleanBuilder();

        // 필터링 조건 추가
        addCategoryFilter(builder, challenge, requestDTO);
        addKeywordFilter(builder, challenge, requestDTO);
        addChallengeStatusFilter(builder, challenge, requestDTO);
        addMembershipFilter(builder, challenge, requestDTO);

        // 쿼리 실행
        var query = queryFactory.selectFrom(challenge).where(builder);
        applySorting(query, challenge, requestDTO);

        List<Challenge> content = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = countTotal(challenge, builder);

        return new PageImpl<>(content, pageable, total);
    }

    private void addCategoryFilter(BooleanBuilder builder, QChallenge challenge,
        ChallengeListReqDTO requestDTO) {
        if (requestDTO.getCategory() != null) {
            builder.and(challenge.category.eq(requestDTO.getCategory()));
        }
    }

    private void addKeywordFilter(BooleanBuilder builder, QChallenge challenge,
        ChallengeListReqDTO requestDTO) {
        if (requestDTO.getKeyword() != null && !requestDTO.getKeyword().trim().isEmpty()) {
            String keyword = "%" + requestDTO.getKeyword().trim() + "%";
            builder.and(
                challenge.title.likeIgnoreCase(keyword)
                    .or(challenge.details.likeIgnoreCase(keyword))
            );
        }
    }

    private void addChallengeStatusFilter(BooleanBuilder builder, QChallenge challenge,
        ChallengeListReqDTO requestDTO) {
        if (requestDTO.getChallengeStatus() != null) {
            builder.and(challenge.status.eq(requestDTO.getChallengeStatus()));
        }
    }

    private void addMembershipFilter(BooleanBuilder builder, QChallenge challenge,
        ChallengeListReqDTO requestDTO) {
        if (requestDTO.getMemberId() == null) {
            return;
        }

        // 멤버의 참여 상태별 필터링 (SUCCESS, FAIL, ONGOING, NOT_YET)
        if (requestDTO.getChallengeMemberStatus() != null) {
            builder.and(
                challenge.challengeMembers.any()
                    .member.id.eq(requestDTO.getMemberId())
                    .and(challenge.challengeMembers.any().challengeMemberStatus.eq(
                        requestDTO.getChallengeMemberStatus()))
            );
        }

        // 참여/미참여 필터링
        if (requestDTO.getIsJoined() != null) {
            if (requestDTO.getIsJoined()) {
                builder.and(
                    challenge.challengeMembers.any().member.id.eq(requestDTO.getMemberId()));
            } else {
                builder.and(
                    challenge.challengeMembers.any().member.id.eq(requestDTO.getMemberId()).not());
            }
        }
    }

    private void applySorting(JPAQuery<Challenge> query, QChallenge challenge,
        ChallengeListReqDTO requestDTO) {
        if (requestDTO.getSortField() == null || requestDTO.getSortDirection() == null) {
            return;
        }

        boolean asc = SortDirection.ASC == requestDTO.getSortDirection();
        switch (requestDTO.getSortField()) {
            case ATTENDEE_COUNT ->
                query.orderBy(asc ? challenge.attendeeCount.asc() : challenge.attendeeCount.desc());
            case COUNT_LIKES ->
                query.orderBy(asc ? challenge.countLikes.asc() : challenge.countLikes.desc());
            case START_DATE ->
                query.orderBy(asc ? challenge.startDate.asc() : challenge.startDate.desc());
            case FINISH_DATE ->
                query.orderBy(asc ? challenge.finishDate.asc() : challenge.finishDate.desc());
        }
    }

    private long countTotal(QChallenge challenge, BooleanBuilder builder) {
        Long count = queryFactory
            .select(Wildcard.count)
            .from(challenge)
            .where(builder)
            .fetchOne();
        return count != null ? count : 0L;
    }
}
