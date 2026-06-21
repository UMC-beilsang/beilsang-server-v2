package site.beilsang.beilsang_server_v2.domain.report.service;

import site.beilsang.beilsang_server_v2.domain.report.dto.ChallengeReportReqDTO;
import site.beilsang.beilsang_server_v2.domain.report.dto.FeedReportReqDTO;

public interface ReportService {

    /**
     * 피드 신고 처리
     *
     * @param memberId 신고한 회원 ID
     * @param feedId   신고 대상 피드 ID
     * @param reqDTO   신고 요청 정보 (사유, 상세)
     */
    void reportFeed(Long memberId, Long feedId, FeedReportReqDTO reqDTO);

    /**
     * 챌린지 신고 처리
     *
     * @param memberId    신고한 회원 ID
     * @param challengeId 신고 대상 챌린지 ID
     * @param reqDTO      신고 요청 정보 (사유, 상세)
     */
    void reportChallenge(Long memberId, Long challengeId, ChallengeReportReqDTO reqDTO);
}
