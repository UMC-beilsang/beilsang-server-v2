package site.beilsang.beilsang_server_v2.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeRepository;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.feed.repository.FeedRepository;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.report.dto.ChallengeReportReqDTO;
import site.beilsang.beilsang_server_v2.domain.report.dto.FeedReportReqDTO;
import site.beilsang.beilsang_server_v2.domain.report.entity.Report;
import site.beilsang.beilsang_server_v2.domain.report.repository.ReportRepository;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.enums.FeedReportReason;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeReportReason;
import site.beilsang.beilsang_server_v2.global.enums.ReportType;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl implements ReportService {

    // 누적 신고 임계치 — 이 값에 도달하면 즉시 숨김 처리
    private static final int REPORT_THRESHOLD = 3;

    private final ReportRepository reportRepository;
    private final FeedRepository feedRepository;
    private final ChallengeRepository challengeRepository;
    private final MemberRepository memberRepository;

    @Override
    public void reportFeed(Long memberId, Long feedId, FeedReportReqDTO reqDTO) {
        // 기타 사유 선택 시 상세 내용 필수
        validateDetail(reqDTO.getReason() == FeedReportReason.ETC, reqDTO.getDetail());

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_FEED));

        // 이미 숨김 처리된 피드인지 확인
        if (feed.getIsHidden()) {
            throw new BaseException(BaseResponseCode.ALREADY_HIDDEN_FEED);
        }

        // 중복 신고 확인
        if (reportRepository.existsByMemberIdAndFeedId(memberId, feedId)) {
            throw new BaseException(BaseResponseCode.ALREADY_REPORTED);
        }

        // 신고 레코드 저장
        reportRepository.save(Report.builder()
            .member(member)
            .feed(feed)
            .reportType(ReportType.FEED)
            .reason(reqDTO.getReason().name())
            .detail(reqDTO.getDetail())
            .build());

        // 신고 수 증가 및 임계치 도달 시 숨김 처리
        feed.incrementReportCount();
        if (feed.getReportCount() >= REPORT_THRESHOLD) {
            feed.hide();
        }
    }

    @Override
    public void reportChallenge(Long memberId, Long challengeId, ChallengeReportReqDTO reqDTO) {
        // 기타 사유 선택 시 상세 내용 필수
        validateDetail(reqDTO.getReason() == ChallengeReportReason.ETC, reqDTO.getDetail());

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_CHALLENGE));

        // 이미 숨김 처리된 챌린지인지 확인
        if (challenge.getIsHidden()) {
            throw new BaseException(BaseResponseCode.ALREADY_HIDDEN_CHALLENGE);
        }

        // 중복 신고 확인
        if (reportRepository.existsByMemberIdAndChallengeId(memberId, challengeId)) {
            throw new BaseException(BaseResponseCode.ALREADY_REPORTED);
        }

        // 신고 레코드 저장
        reportRepository.save(Report.builder()
            .member(member)
            .challenge(challenge)
            .reportType(ReportType.CHALLENGE)
            .reason(reqDTO.getReason().name())
            .detail(reqDTO.getDetail())
            .build());

        // 신고 수 증가 및 임계치 도달 시 챌린지·피드 일괄 숨김 처리
        challenge.incrementReportCount();
        if (challenge.getReportCount() >= REPORT_THRESHOLD) {
            challenge.hide();
            // 해당 챌린지의 모든 피드도 연쇄 숨김 처리
            feedRepository.hideAllByChallengeId(challengeId);
        }
    }

    /**
     * ETC 사유 선택 시 detail 필드 입력 여부 검증
     *
     * @param isEtc   기타 사유 선택 여부
     * @param detail  상세 사유
     */
    private void validateDetail(boolean isEtc, String detail) {
        if (isEtc && (detail == null || detail.isBlank())) {
            throw new BaseException(BaseResponseCode.MISSING_REPORT_DETAIL);
        }
    }
}
