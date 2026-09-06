package site.beilsang.beilsang_server_v2.domain.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeRepository;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.notification.entity.ChallengeNotification;
import site.beilsang.beilsang_server_v2.domain.notification.repository.NotificationRepository;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationScheduler 단위테스트")
class NotificationSchedulerTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private ChallengeMemberRepository challengeMemberRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private FCMService fcmService;

    @InjectMocks
    private NotificationScheduler notificationScheduler;

    private Challenge challenge;
    private ChallengeMember challengeMember1;
    private ChallengeMember challengeMember2;

    @BeforeEach
    void setUp() {
        challenge = Challenge.builder()
            .id(1L)
            .title("테스트 챌린지")
            .category(Category.PLOGGING)
            .startDate(LocalDate.now().plusDays(1))
            .status(ChallengeStatus.NOT_YET)
            .isHidden(false)
            .build();

        Member member1 = Member.builder()
            .id(11L)
            .nickName("멤버1")
            .deviceToken("token-1")
            .build();

        Member member2 = Member.builder()
            .id(12L)
            .nickName("멤버2")
            .deviceToken("token-2")
            .build();

        challengeMember1 = ChallengeMember.builder()
            .id(101L)
            .member(member1)
            .challenge(challenge)
            .challengeMemberStatus(ChallengeMemberStatus.NOT_YET)
            .isFeedUpload(false)
            .successDays(0)
            .build();

        challengeMember2 = ChallengeMember.builder()
            .id(102L)
            .member(member2)
            .challenge(challenge)
            .challengeMemberStatus(ChallengeMemberStatus.ONGOING)
            .isFeedUpload(false)
            .successDays(0)
            .build();
    }

    @Test
    @DisplayName("시작 알림은 NOT_YET와 ONGOING 참여자를 모두 대상으로 발송한다")
    void sendChallengeStartNotification_ShouldIncludeNotYetAndOngoingMembers() {
        // given
        when(challengeRepository.findAllByStartDateAndIsHiddenFalse(LocalDate.now().plusDays(1)))
            .thenReturn(List.of(challenge));
        when(challengeMemberRepository.findAllByChallengeIdAndChallengeMemberStatusIn(
            anyLong(), anyList()))
            .thenReturn(List.of(challengeMember1, challengeMember2));
        when(notificationRepository.existsByChallengeIdAndMemberIdAndCreatedAtAfter(
            anyLong(), anyLong(), any()))
            .thenReturn(false);

        // when
        notificationScheduler.sendChallengeStartNotification();

        // then
        verify(notificationRepository, times(2)).save(any(ChallengeNotification.class));
        verify(fcmService, times(2)).sendToToken(anyString(), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("자정 리셋 스케줄은 진행 중인 챌린지 멤버의 일일 인증 플래그를 초기화한다")
    void resetDailyFeedUploadStatus_ShouldResetOngoingMembers() {
        // given
        when(challengeMemberRepository.resetFeedUploadStatusByChallengeMemberStatus(
            ChallengeMemberStatus.ONGOING)).thenReturn(2);

        // when
        notificationScheduler.resetDailyFeedUploadStatus();

        // then
        verify(challengeMemberRepository).resetFeedUploadStatusByChallengeMemberStatus(
            ChallengeMemberStatus.ONGOING);
    }
}




