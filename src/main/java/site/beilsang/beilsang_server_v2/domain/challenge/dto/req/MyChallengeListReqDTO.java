package site.beilsang.beilsang_server_v2.domain.challenge.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ParticipationStatus;

/**
 * 나의 챌린지 목록 조회 요청 DTO
 * - 내가 참여한 챌린지 목록을 상태별로 조회
 * - 정렬은 참여일(createdAt) 내림차순으로 고정
 */
@Schema(description = "나의 챌린지 목록 조회 요청 DTO")
@Getter
@Setter
public class MyChallengeListReqDTO {

    @Schema(description = "참여 상태 (필수): ONGOING(참여중), SUCCESS(달성), FAIL(실패)",
        example = "ONGOING",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"ONGOING", "SUCCESS", "FAIL"})
    @NotNull(message = "참여 상태는 필수입니다")
    private ParticipationStatus participationStatus;

    @Schema(description = "카테고리 필터 (ALL: 전체 조회)",
        example = "ALL",
        allowableValues = {"ALL", "TUMBLER", "REFILL_STATION", "MULTIPLE_CONTAINERS",
            "ECO_PRODUCT", "PLOGGING", "VEGAN", "PUBLIC_TRANSPORT", "BIKE", "RECYCLE"})
    private Category category = Category.ALL;

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", minimum = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private Integer page = 0;

    @Schema(description = "페이지 크기", example = "10", minimum = "1", maximum = "100")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    private Integer size = 10;
}
