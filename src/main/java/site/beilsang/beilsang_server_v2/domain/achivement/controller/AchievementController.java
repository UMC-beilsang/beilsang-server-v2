package site.beilsang.beilsang_server_v2.domain.achivement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import site.beilsang.beilsang_server_v2.domain.achivement.dto.res.HallOfFameListResDto;
import site.beilsang.beilsang_server_v2.domain.achivement.service.HallOfFameService;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;
import site.beilsang.beilsang_server_v2.global.enums.Category;

@Tag(name = "Achievement", description = "발견 api")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/achievement")
public class AchievementController {

    private final HallOfFameService hallOfFameService;

    @GetMapping("/hall-of-fame/{category}")
    @Operation(summary = "카테고리별 명예의 전당 조회 API",
        description = "카테고리에서 좋아요를 가장 많이 받은 챌린지 1위~10위를 조회하는 API입니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    })
    public BaseResponse<HallOfFameListResDto > getCategoryHallOfFame(
        @Parameter(description = "카테고리 (ALL, TUMBLER, REFILL_STATION, MULTIPLE_CONTAINERS, ECO_PRODUCT, PLOGGING, VEGAN, PUBLIC_TRANSPORT, BIKE, RECYCLE)")
        @PathVariable Category category
    ) {
        HallOfFameListResDto response = hallOfFameService.getCategoryHallOfFame(category);
        return new BaseResponse<>(response);
    }
}
