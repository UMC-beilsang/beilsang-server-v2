package site.beilsang.beilsang_server_v2.domain.challenge.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import site.beilsang.beilsang_server_v2.global.enums.Category;

/**
 * 모집마감 챌린지 목록 조회 요청 DTO
 * - 시작일이 오늘 이전인 챌린지를 대상으로 조회
 * - 정렬은 최근 마감순(startDate DESC)으로 고정
 */
@Schema(description = "모집마감 챌린지 목록 조회 요청 DTO")
@Getter
@Setter
public class ClosedChallengeListReqDTO {

    @Schema(description = "카테고리 필터 (null이면 전체 조회)",
        example = "TUMBLER",
        allowableValues = {"ALL", "TUMBLER", "REFILL_STATION", "MULTIPLE_CONTAINERS",
            "ECO_PRODUCT", "PLOGGING", "VEGAN", "PUBLIC_TRANSPORT", "BIKE", "RECYCLE"})
    private Category category;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", minimum = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "10", minimum = "1", maximum = "100")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    private Integer size = 10;
}