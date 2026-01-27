package site.beilsang.beilsang_server_v2.global.enums;

/**
 * 챌린지 검색 결과 정렬 타입
 * - DEADLINE_SOON: 마감 임박순 (시작일 오름차순)
 * - NEWEST: 최신순 (생성일 내림차순)
 */
public enum SearchSortType {
    DEADLINE_SOON,  // 마감 임박순: startDate 오름차순
    NEWEST          // 최신순: createdAt 내림차순
}