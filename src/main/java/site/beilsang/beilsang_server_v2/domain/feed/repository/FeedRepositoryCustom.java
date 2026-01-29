package site.beilsang.beilsang_server_v2.domain.feed.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.global.enums.FeedSortType;

public interface FeedRepositoryCustom {

    /**
     * 피드 검색 피드 내용(review)을 키워드로 검색하고, 등록 시간 기준으로 정렬합니다.
     *
     * @param keyword  검색 키워드 (review 필드 검색, null인 경우 전체 조회)
     * @param sortType 정렬 타입 (NEWEST: 최신순, OLDEST: 오래된순)
     * @param pageable 페이징 정보
     * @return 검색된 피드 목록 (Slice)
     */
    Slice<Feed> searchFeeds(String keyword, FeedSortType sortType, Pageable pageable);
}
