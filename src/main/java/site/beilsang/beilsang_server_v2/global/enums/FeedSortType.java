package site.beilsang.beilsang_server_v2.global.enums;

/**
 * 피드 검색 결과 정렬 타입
 * - NEWEST: 최신순 (생성일 내림차순)
 * - OLDEST: 오래된순 (생성일 오름차순)
 */
public enum FeedSortType {
    NEWEST,  // 최신순: createdAt 내림차순
    OLDEST   // 오래된순: createdAt 오름차순
}