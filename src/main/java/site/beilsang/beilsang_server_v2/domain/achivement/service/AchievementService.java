package site.beilsang.beilsang_server_v2.domain.achivement.service;

import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.HallOfFameListResDto;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedResDTO;
import site.beilsang.beilsang_server_v2.global.common.SliceResponseDTO;
import site.beilsang.beilsang_server_v2.global.enums.Category;

public interface AchievementService {

    /**
     * 특정 카테고리에 대한 명예의 전당을 조회합니다.
     *
     * @param category 조회할 카테고리
     * @return 해당 카테고리의 명예의 전당 목록 (1위~10위)
     */
    HallOfFameListResDto getCategoryHallOfFame(Category category);

    /**
     * 특정 카테고리에 대한 피드들을 조회합니다.
     *
     * @param category 조회할 카테고리
     * @param page     페이지 번호
     * @param size     페이지 크기
     * @param memberId 요청한 사용자 ID (isMyFeed 계산용)
     * @return 해당 카테고리와 페이지에 해당하는 챌린지 피드 목록 (SliceResponseDTO)
     */
    SliceResponseDTO<PreviewFeedResDTO> getFeedsByCategory(Category category, Integer page,
        Integer size, Long memberId);
}
