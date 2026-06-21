package site.beilsang.beilsang_server_v2.domain.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import site.beilsang.beilsang_server_v2.global.enums.ChallengeReportReason;
import site.beilsang.beilsang_server_v2.global.enums.FeedReportReason;
import site.beilsang.beilsang_server_v2.global.enums.ReportType;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportService 단위테스트")
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Member testMember;
    private Feed testFeed;
    private Challenge testChallenge;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .build();

        testFeed = Feed.builder()
            .build();

        testChallenge = Challenge.builder()
            .build();
    }

    // ========================
    // 피드 신고 테스트
    // ========================

    @Test
    @DisplayName("피드 신고 성공 - report 저장 및 reportCount 증가")
    void reportFeed_success() {
        // given
        FeedReportReqDTO reqDTO = new FeedReportReqDTO();
        reqDTO.setReason(FeedReportReason.SPAM);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(feedRepository.findById(1L)).thenReturn(Optional.of(testFeed));
        when(reportRepository.existsByMemberIdAndFeedId(1L, 1L)).thenReturn(false);

        // when
        reportService.reportFeed(1L, 1L, reqDTO);

        // then
        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getReportType()).isEqualTo(ReportType.FEED);
        assertThat(captor.getValue().getReason()).isEqualTo(FeedReportReason.SPAM.name());
        assertThat(testFeed.getReportCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("피드 신고 3회 누적 시 isHidden = true 처리")
    void reportFeed_hideWhenThresholdReached() {
        // given — reportCount를 2로 설정하여 이번 신고가 3번째가 되도록
        Feed feedWithTwoReports = Feed.builder().build();
        feedWithTwoReports.incrementReportCount();
        feedWithTwoReports.incrementReportCount();

        FeedReportReqDTO reqDTO = new FeedReportReqDTO();
        reqDTO.setReason(FeedReportReason.SPAM);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(feedRepository.findById(1L)).thenReturn(Optional.of(feedWithTwoReports));
        when(reportRepository.existsByMemberIdAndFeedId(1L, 1L)).thenReturn(false);

        // when
        reportService.reportFeed(1L, 1L, reqDTO);

        // then
        assertThat(feedWithTwoReports.getReportCount()).isEqualTo(3);
        assertThat(feedWithTwoReports.getIsHidden()).isTrue();
    }

    @Test
    @DisplayName("피드 신고 - 중복 신고 시 ALREADY_REPORTED 예외")
    void reportFeed_alreadyReported() {
        // given
        FeedReportReqDTO reqDTO = new FeedReportReqDTO();
        reqDTO.setReason(FeedReportReason.SPAM);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(feedRepository.findById(1L)).thenReturn(Optional.of(testFeed));
        when(reportRepository.existsByMemberIdAndFeedId(1L, 1L)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> reportService.reportFeed(1L, 1L, reqDTO))
            .isInstanceOf(BaseException.class)
            .satisfies(e -> assertThat(((BaseException) e).getBaseResponseCode())
                .isEqualTo(BaseResponseCode.ALREADY_REPORTED));

        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("피드 신고 - 이미 숨김 처리된 피드 신고 시 ALREADY_HIDDEN_FEED 예외")
    void reportFeed_alreadyHidden() {
        // given
        Feed hiddenFeed = Feed.builder().build();
        hiddenFeed.hide();

        FeedReportReqDTO reqDTO = new FeedReportReqDTO();
        reqDTO.setReason(FeedReportReason.SPAM);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(feedRepository.findById(1L)).thenReturn(Optional.of(hiddenFeed));

        // when & then
        assertThatThrownBy(() -> reportService.reportFeed(1L, 1L, reqDTO))
            .isInstanceOf(BaseException.class)
            .satisfies(e -> assertThat(((BaseException) e).getBaseResponseCode())
                .isEqualTo(BaseResponseCode.ALREADY_HIDDEN_FEED));

        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("피드 신고 - ETC 사유에 detail 미입력 시 MISSING_REPORT_DETAIL 예외")
    void reportFeed_etcWithoutDetail() {
        // given
        FeedReportReqDTO reqDTO = new FeedReportReqDTO();
        reqDTO.setReason(FeedReportReason.ETC);
        reqDTO.setDetail(null);

        // when & then
        assertThatThrownBy(() -> reportService.reportFeed(1L, 1L, reqDTO))
            .isInstanceOf(BaseException.class)
            .satisfies(e -> assertThat(((BaseException) e).getBaseResponseCode())
                .isEqualTo(BaseResponseCode.MISSING_REPORT_DETAIL));

        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("피드 신고 - ETC 사유에 공백만 입력 시 MISSING_REPORT_DETAIL 예외")
    void reportFeed_etcWithBlankDetail() {
        // given
        FeedReportReqDTO reqDTO = new FeedReportReqDTO();
        reqDTO.setReason(FeedReportReason.ETC);
        reqDTO.setDetail("   ");

        // when & then
        assertThatThrownBy(() -> reportService.reportFeed(1L, 1L, reqDTO))
            .isInstanceOf(BaseException.class)
            .satisfies(e -> assertThat(((BaseException) e).getBaseResponseCode())
                .isEqualTo(BaseResponseCode.MISSING_REPORT_DETAIL));
    }

    // ========================
    // 챌린지 신고 테스트
    // ========================

    @Test
    @DisplayName("챌린지 신고 성공 - report 저장 및 reportCount 증가")
    void reportChallenge_success() {
        // given
        ChallengeReportReqDTO reqDTO = new ChallengeReportReqDTO();
        reqDTO.setReason(ChallengeReportReason.SPAM);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(challengeRepository.findById(1L)).thenReturn(Optional.of(testChallenge));
        when(reportRepository.existsByMemberIdAndChallengeId(1L, 1L)).thenReturn(false);

        // when
        reportService.reportChallenge(1L, 1L, reqDTO);

        // then
        ArgumentCaptor<Report> captor = ArgumentCaptor.forClass(Report.class);
        verify(reportRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getReportType()).isEqualTo(ReportType.CHALLENGE);
        assertThat(captor.getValue().getReason()).isEqualTo(ChallengeReportReason.SPAM.name());
        assertThat(testChallenge.getReportCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("챌린지 신고 3회 누적 시 챌린지·피드 일괄 숨김 처리")
    void reportChallenge_hideWithFeedsWhenThresholdReached() {
        // given — reportCount를 2로 설정하여 이번 신고가 3번째가 되도록
        Challenge challengeWithTwoReports = Challenge.builder().build();
        challengeWithTwoReports.incrementReportCount();
        challengeWithTwoReports.incrementReportCount();

        ChallengeReportReqDTO reqDTO = new ChallengeReportReqDTO();
        reqDTO.setReason(ChallengeReportReason.SPAM);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(challengeRepository.findById(1L)).thenReturn(Optional.of(challengeWithTwoReports));
        when(reportRepository.existsByMemberIdAndChallengeId(1L, 1L)).thenReturn(false);

        // when
        reportService.reportChallenge(1L, 1L, reqDTO);

        // then
        assertThat(challengeWithTwoReports.getReportCount()).isEqualTo(3);
        assertThat(challengeWithTwoReports.getIsHidden()).isTrue();
        // 피드 일괄 숨김 쿼리 호출 확인
        verify(feedRepository, times(1)).hideAllByChallengeId(1L);
    }

    @Test
    @DisplayName("챌린지 신고 - 중복 신고 시 ALREADY_REPORTED 예외")
    void reportChallenge_alreadyReported() {
        // given
        ChallengeReportReqDTO reqDTO = new ChallengeReportReqDTO();
        reqDTO.setReason(ChallengeReportReason.SPAM);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(challengeRepository.findById(1L)).thenReturn(Optional.of(testChallenge));
        when(reportRepository.existsByMemberIdAndChallengeId(1L, 1L)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> reportService.reportChallenge(1L, 1L, reqDTO))
            .isInstanceOf(BaseException.class)
            .satisfies(e -> assertThat(((BaseException) e).getBaseResponseCode())
                .isEqualTo(BaseResponseCode.ALREADY_REPORTED));

        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("챌린지 신고 - 이미 숨김 처리된 챌린지 신고 시 ALREADY_HIDDEN_CHALLENGE 예외")
    void reportChallenge_alreadyHidden() {
        // given
        Challenge hiddenChallenge = Challenge.builder().build();
        hiddenChallenge.hide();

        ChallengeReportReqDTO reqDTO = new ChallengeReportReqDTO();
        reqDTO.setReason(ChallengeReportReason.SPAM);

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(challengeRepository.findById(1L)).thenReturn(Optional.of(hiddenChallenge));

        // when & then
        assertThatThrownBy(() -> reportService.reportChallenge(1L, 1L, reqDTO))
            .isInstanceOf(BaseException.class)
            .satisfies(e -> assertThat(((BaseException) e).getBaseResponseCode())
                .isEqualTo(BaseResponseCode.ALREADY_HIDDEN_CHALLENGE));

        verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("챌린지 신고 - ETC 사유에 detail 미입력 시 MISSING_REPORT_DETAIL 예외")
    void reportChallenge_etcWithoutDetail() {
        // given
        ChallengeReportReqDTO reqDTO = new ChallengeReportReqDTO();
        reqDTO.setReason(ChallengeReportReason.ETC);
        reqDTO.setDetail(null);

        // when & then
        assertThatThrownBy(() -> reportService.reportChallenge(1L, 1L, reqDTO))
            .isInstanceOf(BaseException.class)
            .satisfies(e -> assertThat(((BaseException) e).getBaseResponseCode())
                .isEqualTo(BaseResponseCode.MISSING_REPORT_DETAIL));

        verify(reportRepository, never()).save(any());
    }
}
