package site.beilsang.beilsang_server_v2.domain.challenge.dto.req;

import lombok.Getter;
import lombok.Setter;
import site.beilsang.beilsang_server_v2.global.enums.SearchSortType;

/**
 * 챌린지 검색 요청 DTO
 * - 모집 마감/모집 중 챌린지 검색 시 사용
 */
@Getter
@Setter
public class SearchChallengeReqDTO {

    // 검색 키워드 (챌린지 제목 검색)
    private String keyword;

    // 정렬 타입 (모집 중인 챌린지 검색 시 사용, 기본값: 마감 임박순)
    private SearchSortType sortType = SearchSortType.DEADLINE_SOON;

    // 페이지 번호 (0부터 시작, 기본값: 0)
    private Integer page = 0;

    // 페이지 크기 (기본값: 10)
    private Integer size = 10;
}
