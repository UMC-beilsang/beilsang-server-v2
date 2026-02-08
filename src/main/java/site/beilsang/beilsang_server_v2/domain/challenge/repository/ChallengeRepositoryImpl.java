package site.beilsang.beilsang_server_v2.domain.challenge.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ClosedChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.LikedChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.OpenChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.QChallenge;
import site.beilsang.beilsang_server_v2.domain.like.entity.QChallengeLike;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.SearchSortType;
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

    /**
     * 모집중인 챌린지 목록 조회
     * - 시작일이 오늘 이후인 챌린지를 대상으로 조회
     * - 카테고리 필터링 및 정렬 기능 제공 (마감 임박순/최신순)
     */
    @Override
    public Page<Challenge> findOpenChallenges(OpenChallengeListReqDTO requestDTO,
        Pageable pageable) {
        QChallenge challenge = QChallenge.challenge;
        BooleanBuilder builder = new BooleanBuilder();

        // 모집중 조건: 시작일이 오늘 이후 (오늘 포함)
        builder.and(challenge.startDate.goe(LocalDate.now()));

        // 카테고리 필터 (ALL이 아닌 경우에만 필터링)
        if (requestDTO.getCategory() != Category.ALL) {
            builder.and(challenge.category.eq(requestDTO.getCategory()));
        }

        // 쿼리 생성
        JPAQuery<Challenge> query = queryFactory
            .selectFrom(challenge)
            .where(builder);

        // 정렬 적용
        if (requestDTO.getSortType() == SearchSortType.NEWEST) {
            // 최신순: 생성일 내림차순
            query.orderBy(challenge.createdAt.desc());
        } else {
            // 마감 임박순 (기본값): 시작일 오름차순
            query.orderBy(challenge.startDate.asc());
        }

        List<Challenge> content = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = countTotal(challenge, builder);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 모집마감된 챌린지 목록 조회
     * - 시작일이 오늘 이전인 챌린지를 대상으로 조회
     * - 최근 마감순(startDate DESC)으로 정렬
     */
    @Override
    public Page<Challenge> findClosedChallenges(ClosedChallengeListReqDTO requestDTO,
        Pageable pageable) {
        QChallenge challenge = QChallenge.challenge;
        BooleanBuilder builder = new BooleanBuilder();

        // 모집마감 조건: 시작일이 오늘 이전
        builder.and(challenge.startDate.lt(LocalDate.now()));

        // 카테고리 필터 (ALL이 아닌 경우에만 필터링)
        if (requestDTO.getCategory() != Category.ALL) {
            builder.and(challenge.category.eq(requestDTO.getCategory()));
        }

        // 쿼리 실행 - 최근 마감순 (startDate 내림차순)
        List<Challenge> content = queryFactory
            .selectFrom(challenge)
            .where(builder)
            .orderBy(challenge.startDate.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = countTotal(challenge, builder);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 찜한 챌린지 목록 조회
     * - 특정 회원이 찜한 챌린지 목록을 조회
     * - 카테고리 필터링 및 정렬 기능 제공 (마감 임박순/최신순)
     */
    @Override
    public Page<Challenge> findLikedChallenges(Long memberId, LikedChallengeListReqDTO requestDTO,
        Pageable pageable) {
        QChallenge challenge = QChallenge.challenge;
        QChallengeLike challengeLike = QChallengeLike.challengeLike;
        BooleanBuilder builder = new BooleanBuilder();

        // 카테고리 필터 (ALL이 아닌 경우에만 필터링)
        if (requestDTO.getCategory() != Category.ALL) {
            builder.and(challenge.category.eq(requestDTO.getCategory()));
        }

        // 쿼리 생성 - ChallengeLike 조인
        JPAQuery<Challenge> query = queryFactory
            .selectFrom(challenge)
            .innerJoin(challengeLike).on(challengeLike.challenge.eq(challenge))
            .where(challengeLike.member.id.eq(memberId).and(builder));

        // 정렬 적용
        if (requestDTO.getSortType() == SearchSortType.NEWEST) {
            // 최신순: 시작일 내림차순
            query.orderBy(challenge.startDate.desc());
        } else {
            // 마감 임박순 (기본값): 시작일 오름차순
            query.orderBy(challenge.startDate.asc());
        }

        List<Challenge> content = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 전체 개수 조회
        Long total = queryFactory
            .select(challenge.count())
            .from(challenge)
            .innerJoin(challengeLike).on(challengeLike.challenge.eq(challenge))
            .where(challengeLike.member.id.eq(memberId).and(builder))
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * 모집 마감 챌린지 검색
     * - 시작일이 오늘 이전인 챌린지를 검색
     * - 오늘 날짜에 가까운 순으로 정렬 (startDate 내림차순)
     */
    @Override
    public Page<Challenge> searchClosedChallenges(String keyword, Pageable pageable) {
        QChallenge challenge = QChallenge.challenge;
        BooleanBuilder builder = new BooleanBuilder();

        // 모집 마감 조건: 시작일이 오늘 이전
        builder.and(challenge.startDate.lt(LocalDate.now()));

        // 제목 키워드 필터
        addTitleKeywordFilter(builder, challenge, keyword);

        // 쿼리 실행 - 오늘 날짜에 가까운 순 (startDate 내림차순)
        List<Challenge> content = queryFactory
            .selectFrom(challenge)
            .where(builder)
            .orderBy(challenge.startDate.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = countTotal(challenge, builder);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 모집 중인 챌린지 검색
     * - 시작일이 오늘 이후인 챌린지를 검색
     * - 정렬: 마감 임박순(DEADLINE_SOON) 또는 최신순(NEWEST)
     */
    @Override
    public Page<Challenge> searchOpenChallenges(String keyword, SearchSortType sortType,
        Pageable pageable) {
        QChallenge challenge = QChallenge.challenge;
        BooleanBuilder builder = new BooleanBuilder();

        // 모집 중 조건: 시작일이 오늘 이후 (오늘 포함)
        builder.and(challenge.startDate.goe(LocalDate.now()));

        // 제목 키워드 필터
        addTitleKeywordFilter(builder, challenge, keyword);

        // 쿼리 생성
        JPAQuery<Challenge> query = queryFactory
            .selectFrom(challenge)
            .where(builder);

        // 정렬 적용
        if (sortType == SearchSortType.NEWEST) {
            // 최신순: 생성일 내림차순
            query.orderBy(challenge.createdAt.desc());
        } else {
            // 마감 임박순 (기본값): 시작일 오름차순
            query.orderBy(challenge.startDate.asc());
        }

        List<Challenge> content = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = countTotal(challenge, builder);

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 제목 키워드 필터 추가 (제목만 검색)
     */
    private void addTitleKeywordFilter(BooleanBuilder builder, QChallenge challenge,
        String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim() + "%";
            builder.and(challenge.title.likeIgnoreCase(likeKeyword));
        }
    }
}
