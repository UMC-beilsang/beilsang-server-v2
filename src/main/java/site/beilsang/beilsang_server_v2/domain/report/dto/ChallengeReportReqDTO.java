package site.beilsang.beilsang_server_v2.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeReportReason;

@Schema(description = "챌린지 신고 요청 DTO")
@Getter
@Setter
public class ChallengeReportReqDTO {

    @Schema(
        description = "신고 사유",
        example = "SPAM",
        allowableValues = {"UNRELATED", "SPAM", "INAPPROPRIATE", "ILLEGAL", "ETC"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "신고 사유는 필수입니다.")
    private ChallengeReportReason reason;

    @Schema(
        description = "기타 신고 사유 직접 입력 (reason이 ETC인 경우 필수)",
        example = "기타 신고 사유입니다."
    )
    private String detail;
}
