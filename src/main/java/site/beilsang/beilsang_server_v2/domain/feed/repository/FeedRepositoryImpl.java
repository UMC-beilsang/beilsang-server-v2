package site.beilsang.beilsang_server_v2.domain.feed.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.feed.entity.QFeed;
import site.beilsang.beilsang_server_v2.global.enums.FeedSortType;

/**
 * Feed Repository의 Custom 구현체
 * QueryDSL을 사용한 복잡한 쿼리를 구현합니다.
 */
@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * 피드 검색
     * 피드 내용(review)을 키워드로 검색하고, 등록 시간 기준으로 정렬합니다.
     *
     * @param keyword  검색 키워드 (review 필드 검색, null인 경우 전체 조회)
     * @param sortType 정렬 타입 (NEWEST: 최신순, OLDEST: 오래된순)
     * @param pageable 페이징 정보
     * @return 검색된 피드 목록 (Slice)
     */
    @Override
    public Slice<Feed> searchFeeds(String keyword, FeedSortType sortType, Pageable pageable) {
        QFeed feed = QFeed.feed;
        BooleanBuilder builder = new BooleanBuilder();

        // 키워드 필터링 (review 필드 검색)
        addKeywordFilter(builder, feed, keyword);

        // 숨김 처리된 피드 제외
        builder.and(feed.isHidden.isFalse());

        // 쿼리 생성
        JPAQuery<Feed> query = queryFactory
            .selectFrom(feed)
            .where(builder);

        // 정렬 적용
        applySorting(query, feed, sortType);

        // limit + 1 방식으로 hasNext 판단
        List<Feed> content = query
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1)  // 하나 더 조회
            .fetch();

        // hasNext 계산
        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            hasNext = true;
            content.remove(pageable.getPageSize());  // 마지막 요소 제거
        }

        // Slice 생성 및 반환
        return new SliceImpl<>(content, pageable, hasNext);
    }

    /**
     * 키워드 필터 추가
     * review 필드에 대해 대소문자 구분 없이 검색합니다.
     *
     * @param builder BooleanBuilder
     * @param feed    QFeed
     * @param keyword 검색 키워드
     */
    private void addKeywordFilter(BooleanBuilder builder, QFeed feed, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword.trim() + "%";
            builder.and(feed.review.likeIgnoreCase(likeKeyword));
        }
    }

    /**
     * 정렬 적용
     * sortType에 따라 createdAt 기준으로 정렬합니다.
     *
     * @param query    JPAQuery
     * @param feed     QFeed
     * @param sortType 정렬 타입
     */
    private void applySorting(JPAQuery<Feed> query, QFeed feed, FeedSortType sortType) {
        if (sortType == FeedSortType.OLDEST) {
            // 오래된순: createdAt 오름차순
            query.orderBy(feed.createdAt.asc());
        } else {
            // 최신순 (기본값): createdAt 내림차순
            query.orderBy(feed.createdAt.desc());
        }
    }
}