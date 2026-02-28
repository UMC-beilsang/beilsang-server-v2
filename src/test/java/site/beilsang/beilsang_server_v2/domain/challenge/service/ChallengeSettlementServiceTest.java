package site.beilsang.beilsang_server_v2.domain.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.point.service.PointService;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;
import site.beilsang.beilsang_server_v2.global.enums.PointName;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeSettlementService 단위테스트")
class ChallengeSettlementServiceTest {

    @Mock
    private ChallengeMemberRepository challengeMemberRepository;

    @Mock
    private PointService pointService;

    @InjectMocks
    private ChallengeSettlementService challengeSettlementService;

    private Challenge challenge;
    private Member member1;
    private Member member2;
    private Member member3;

    @BeforeEach
    void setUp() {
        // 테스트용 챌린지 (참가비 100, 목표 5일, 3명 참여 → collectedPoint 300)
        challenge = Challenge.builder()
            .id(1L)
            .title("테스트 챌린지")
            .totalGoalDay(5)
            .joinPoint(100)
            .collectedPoint(300)
            .attendeeCount(3)
            .isSettled(false)
            .build();

        member1 = Member.builder().id(1L).nickName("멤버1").point(0).build();
        member2 = Member.builder().id(2L).nickName("멤버2").point(0).build();
        member3 = Member.builder().id(3L).nickName("멤버3").point(0).build();
    }

    @Test
    @DisplayName("전원 성공 — collectedPoint를 성공 멤버 수로 균등 분배")
    void settleChallenge_AllSuccess() {
        // given
        List<ChallengeMember> ongoingMembers = List.of(
            createChallengeMember(member1, false, 5),  // 목표 달성
            createChallengeMember(member2, false, 7),  // 목표 초과 달성
            createChallengeMember(member3, true, 5)    // 호스트, 목표 달성
        );

        when(challengeMemberRepository.findAllByChallengeIdAndChallengeMemberStatus(
            1L, ChallengeMemberStatus.ONGOING)).thenReturn(ongoingMembers);

        // when
        challengeSettlementService.settleChallenge(challenge);

        // then — 3명 전원 SUCCESS, 1인당 100포인트(300/3)
        assertThat(ongoingMembers).allMatch(
            m -> m.getChallengeMemberStatus() == ChallengeMemberStatus.SUCCESS);
        verify(pointService, times(3)).grantChallengePoints(
            any(Member.class), eq(challenge), eq(100), eq(PointName.SUCCESS_CHALLENGE));
        assertThat(challenge.getIsSettled()).isTrue();
    }

    @Test
    @DisplayName("일부 성공 — 성공 멤버에게만 포인트 분배, 실패 멤버는 FAIL 상태")
    void settleChallenge_PartialSuccess() {
        // given
        ChallengeMember successMember = createChallengeMember(member1, false, 5);
        ChallengeMember failMember1 = createChallengeMember(member2, false, 3);
        ChallengeMember failMember2 = createChallengeMember(member3, true, 2);  // 호스트이지만 실패
        List<ChallengeMember> ongoingMembers = List.of(successMember, failMember1, failMember2);

        when(challengeMemberRepository.findAllByChallengeIdAndChallengeMemberStatus(
            1L, ChallengeMemberStatus.ONGOING)).thenReturn(ongoingMembers);

        // when
        challengeSettlementService.settleChallenge(challenge);

        // then — 성공 1명에게 300포인트 전액 지급
        assertThat(successMember.getChallengeMemberStatus()).isEqualTo(ChallengeMemberStatus.SUCCESS);
        assertThat(failMember1.getChallengeMemberStatus()).isEqualTo(ChallengeMemberStatus.FAIL);
        assertThat(failMember2.getChallengeMemberStatus()).isEqualTo(ChallengeMemberStatus.FAIL);
        verify(pointService, times(1)).grantChallengePoints(
            eq(member1), eq(challenge), eq(300), eq(PointName.SUCCESS_CHALLENGE));
        assertThat(challenge.getIsSettled()).isTrue();
    }

    @Test
    @DisplayName("전원 실패 — 포인트 분배 없음, collectedPoint 소멸")
    void settleChallenge_AllFail() {
        // given
        List<ChallengeMember> ongoingMembers = List.of(
            createChallengeMember(member1, false, 2),
            createChallengeMember(member2, false, 0),
            createChallengeMember(member3, true, 4)   // 호스트이지만 목표 미달
        );

        when(challengeMemberRepository.findAllByChallengeIdAndChallengeMemberStatus(
            1L, ChallengeMemberStatus.ONGOING)).thenReturn(ongoingMembers);

        // when
        challengeSettlementService.settleChallenge(challenge);

        // then — 전원 FAIL, 포인트 지급 없음
        assertThat(ongoingMembers).allMatch(
            m -> m.getChallengeMemberStatus() == ChallengeMemberStatus.FAIL);
        verifyNoInteractions(pointService);
        assertThat(challenge.getIsSettled()).isTrue();
    }

    @Test
    @DisplayName("중복 정산 방지 — isSettled가 true이면 정산 스킵")
    void settleChallenge_AlreadySettled() {
        // given
        Challenge settledChallenge = Challenge.builder()
            .id(2L)
            .totalGoalDay(5)
            .collectedPoint(300)
            .isSettled(true)
            .build();

        // when
        challengeSettlementService.settleChallenge(settledChallenge);

        // then — Repository, PointService 모두 호출되지 않음
        verifyNoInteractions(challengeMemberRepository, pointService);
    }

    @Test
    @DisplayName("나머지 포인트 처리 — 나누어떨어지지 않는 경우 나머지 소멸")
    void settleChallenge_RemainderPointDiscarded() {
        // given — collectedPoint 300, 성공 멤버 2명 → 1인당 150, 나머지 0
        //         collectedPoint 를 301로 변경 → 1인당 150, 나머지 1 소멸
        Challenge oddChallenge = Challenge.builder()
            .id(3L)
            .totalGoalDay(5)
            .collectedPoint(301)
            .isSettled(false)
            .build();

        List<ChallengeMember> ongoingMembers = List.of(
            createChallengeMember(member1, false, 5),
            createChallengeMember(member2, false, 6)
        );

        when(challengeMemberRepository.findAllByChallengeIdAndChallengeMemberStatus(
            3L, ChallengeMemberStatus.ONGOING)).thenReturn(ongoingMembers);

        // when
        challengeSettlementService.settleChallenge(oddChallenge);

        // then — 1인당 150포인트 (301/2=150, 나머지 1 소멸)
        verify(pointService, times(2)).grantChallengePoints(
            any(Member.class), eq(oddChallenge), eq(150), eq(PointName.SUCCESS_CHALLENGE));
        assertThat(oddChallenge.getIsSettled()).isTrue();
    }

    @Test
    @DisplayName("ONGOING 멤버가 없는 경우 — 정산 건너뛰고 isSettled 처리")
    void settleChallenge_NoOngoingMembers() {
        // given
        when(challengeMemberRepository.findAllByChallengeIdAndChallengeMemberStatus(
            1L, ChallengeMemberStatus.ONGOING)).thenReturn(Collections.emptyList());

        // when
        challengeSettlementService.settleChallenge(challenge);

        // then
        verifyNoInteractions(pointService);
        assertThat(challenge.getIsSettled()).isTrue();
    }

    /**
     * 테스트용 ChallengeMember 생성 헬퍼 메서드
     */
    private ChallengeMember createChallengeMember(Member member, boolean isHost, int successDays) {
        return ChallengeMember.builder()
            .member(member)
            .challenge(challenge)
            .isHost(isHost)
            .successDays(successDays)
            .challengeMemberStatus(ChallengeMemberStatus.ONGOING)
            .isFeedUpload(false)
            .build();
    }
}