package site.beilsang.beilsang_server_v2.domain.challenge.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import site.beilsang.beilsang_server_v2.global.enums.SearchSortType;

@Schema(description = "모집 중인 챌린지 검색 요청 DTO")
@Getter
@Setter
public class SearchOpenChallengeReqDTO {

    @Schema(description = "검색 키워드 (챌린지 제목 검색)", example = "플로깅")
    private String keyword;

    @Schema(description = "정렬 타입",
        example = "DEADLINE_SOON",
        allowableValues = {"DEADLINE_SOON", "NEWEST"})
    private SearchSortType sortType = SearchSortType.DEADLINE_SOON;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", minimum = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "10", minimum = "1", maximum = "100")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    private Integer size = 10;
}
