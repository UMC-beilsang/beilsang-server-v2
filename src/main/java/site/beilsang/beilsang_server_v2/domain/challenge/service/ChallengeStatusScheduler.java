package site.beilsang.beilsang_server_v2.domain.challenge.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeRepository;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeStatusScheduler {

    private final ChallengeRepository challengeRepository;
    private final ChallengeSettlementService challengeSettlementService;
    private final ChallengeMemberRepository challengeMemberRepository;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    public void updateChallengeStatuses() {
        log.info("챌린지 상태 업데이트 스케줄러 실행 시작");

        LocalDate today = LocalDate.now();

        // 모든 챌린지 조회
        List<Challenge> challenges = challengeRepository.findAll();

        int updatedCount = 0;

        for (Challenge challenge : challenges) {
            ChallengeStatus currentStatus = challenge.getStatus();
            ChallengeStatus calculatedStatus = challenge.calculateCurrentStatus(today);

            // 상태가 변경된 경우에만 업데이트
            if (currentStatus != calculatedStatus) {
                challenge.updateStatus(calculatedStatus);
                updatedCount++;
                log.debug("챌린지 ID: {}, 상태 변경: {} -> {}",
                    challenge.getId(), currentStatus, calculatedStatus);

                // IN_PROGRESS 전환 시 ChallengeMember 상태 동기화
                if (calculatedStatus == ChallengeStatus.IN_PROGRESS) {
                    promoteNotYetMembers(challenge);
                }

                // END 상태로 전환된 챌린지에 대해 포인트 정산 수행
                if (calculatedStatus == ChallengeStatus.END) {
                    // 서버 다운 등으로 NOT_YET → ONGOING 전환이 누락된 멤버 catch-up
                    promoteNotYetMembers(challenge);
                    challengeSettlementService.settleChallenge(challenge);
                }
            }
        }

        log.info("챌린지 상태 업데이트 완료. 총 {}개 챌린지 상태 변경", updatedCount);
    }

    private void promoteNotYetMembers(Challenge challenge) {
        List<ChallengeMember> notYetMembers = challengeMemberRepository
            .findAllByChallengeIdAndChallengeMemberStatus(
                challenge.getId(), ChallengeMemberStatus.NOT_YET);
        if (!notYetMembers.isEmpty()) {
            notYetMembers.forEach(member ->
                member.updateChallengeMemberStatus(ChallengeMemberStatus.ONGOING));
            log.debug("챌린지 ID: {}, ChallengeMember {}명 NOT_YET → ONGOING 전환",
                challenge.getId(), notYetMembers.size());
        }
    }
}
