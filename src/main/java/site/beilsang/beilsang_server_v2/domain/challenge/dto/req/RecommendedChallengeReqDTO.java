package site.beilsang.beilsang_server_v2.domain.challenge.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * 추천 챌린지 조회 요청 DTO
 * - 현재 모집 중인 챌린지 중 좋아요 순으로 조회
 * - 페이지네이션 없이 상위 N개만 조회
 */
@Schema(description = "추천 챌린지 조회 요청 DTO")
@Getter
@Setter
public class RecommendedChallengeReqDTO {

    @Schema(description = "조회할 챌린지 개수", example = "10", minimum = "1", maximum = "50")
    @Min(value = 1, message = "조회 개수는 1 이상이어야 합니다")
    @Max(value = 50, message = "조회 개수는 50 이하여야 합니다")
    private Integer size = 10;
}