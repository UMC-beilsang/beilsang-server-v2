package site.beilsang.beilsang_server_v2.domain.feed.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import site.beilsang.beilsang_server_v2.global.enums.FeedSortType;

/**
 * 피드 검색 요청 DTO
 * 피드 내용(review)을 키워드로 검색하고, 등록 시간 기준으로 정렬합니다.
 */
@Schema(description = "피드 검색 요청 DTO")
@Getter
@Setter
public class FeedSearchReqDTO {

    @Schema(description = "검색 키워드 (피드 내용 검색)", example = "오늘의 플로깅")
    private String keyword;

    @Schema(description = "정렬 타입",
        example = "NEWEST",
        allowableValues = {"NEWEST", "OLDEST"})
    private FeedSortType sortType = FeedSortType.NEWEST;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", minimum = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "4", minimum = "1", maximum = "10")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 10, message = "페이지 크기는 10 이하여야 합니다")
    private Integer size = 4;
}
