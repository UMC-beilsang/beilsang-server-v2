package site.beilsang.beilsang_server_v2.domain.badge.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeBadgeType;

import java.time.LocalDateTime;

@Schema(description = "배지 응답 DTO")
@Builder
@Getter
public class BadgeResDTO {

    @Schema(description = "배지 멤버 ID", example = "1")
    private Long badgeMemberId;

    @Schema(description = "배지 ID", example = "1")
    private Long badgeId;

    @Schema(description = "활동 배지 타입", example = "CHALLENGE_START")
    private ChallengeBadgeType badgeType;  // 챌린지 배지는 null

    @Schema(description = "챌린지 배지 타입", example = "텀블러")
    private Category category;    // 활동 배지는 null

    @Schema(description = "챌린지 배지 단계", example = "1~4")
    private Integer currentStep;  // 활동 배지는 null

    @Schema(description = "획득 시간")
    private LocalDateTime acquiredAt;
}
