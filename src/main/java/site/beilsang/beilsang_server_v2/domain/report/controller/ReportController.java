package site.beilsang.beilsang_server_v2.domain.report.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import site.beilsang.beilsang_server_v2.domain.report.dto.ChallengeReportReqDTO;
import site.beilsang.beilsang_server_v2.domain.report.dto.FeedReportReqDTO;
import site.beilsang.beilsang_server_v2.domain.report.service.ReportService;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;

@RestController
@RequiredArgsConstructor
@Tag(name = "Report", description = "신고 API")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "피드 신고", description = "인증 피드를 신고합니다. 누적 3회 도달 시 자동으로 숨김 처리됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "피드 신고 성공"),
        @ApiResponse(responseCode = "400", description = "이미 신고한 피드 / 이미 숨김 처리된 피드 / 기타 사유 미입력"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "피드를 찾을 수 없음")
    })
    @PostMapping("/feed/{feedId}/report")
    public BaseResponse<Void> reportFeed(
        @Parameter(description = "신고할 피드 ID", example = "1") @PathVariable Long feedId,
        @Valid @RequestBody FeedReportReqDTO reqDTO,
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        reportService.reportFeed(memberId, feedId, reqDTO);
        return new BaseResponse<>();
    }

    @Operation(summary = "챌린지 신고", description = "챌린지를 신고합니다. 누적 3회 도달 시 챌린지 및 해당 챌린지의 모든 피드가 자동으로 숨김 처리됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "챌린지 신고 성공"),
        @ApiResponse(responseCode = "400", description = "이미 신고한 챌린지 / 이미 숨김 처리된 챌린지 / 기타 사유 미입력"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음")
    })
    @PostMapping("/challenge/{challengeId}/report")
    public BaseResponse<Void> reportChallenge(
        @Parameter(description = "신고할 챌린지 ID", example = "1") @PathVariable Long challengeId,
        @Valid @RequestBody ChallengeReportReqDTO reqDTO,
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        reportService.reportChallenge(memberId, challengeId, reqDTO);
        return new BaseResponse<>();
    }
}
