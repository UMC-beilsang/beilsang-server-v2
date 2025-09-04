package site.beilsang.beilsang_server_v2.domain.achivement.service;

import site.beilsang.beilsang_server_v2.domain.achivement.dto.res.HallOfFameListResDto;
import site.beilsang.beilsang_server_v2.global.enums.Category;

public interface HallOfFameService {

    /**
     * 특정 카테고리에 대한 명예의 전당을 조회합니다.
     * @param category 조회할 카테고리
     * @return 해당 카테고리의 명예의 전당 목록 (1위~10위)
     */
    HallOfFameListResDto getCategoryHallOfFame(Category category);
}
