package site.beilsang.beilsang_server_v2.domain.challenge.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.beilsang.beilsang_server_v2.domain.badge.service.BadgeService;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.point.service.PointService;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;
import site.beilsang.beilsang_server_v2.global.enums.PointName;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeSettlementService {

    private final ChallengeMemberRepository challengeMemberRepository;
    private final PointService pointService;
    private final BadgeService badgeService;

    // TODO: 호스트 보너스 정책 미확정 — 추후 논의 안건 상정 필요
    private static final int HOST_BONUS_POINT = 0;

    /**
     * 챌린지 종료 시 포인트 정산을 수행합니다.
     *
     * 1. 중복 정산 확인
     * 2. ONGOING 멤버들의 성공/실패 판정
     * 3. 성공 멤버에게 collectedPoint 균등 분배
     * 4. 호스트가 성공한 경우 보너스 포인트 지급
     * 5. 정산 완료 표시
     *
     * @param challenge 정산 대상 챌린지 (상태가 END로 전환된 챌린지)
     */
    public void settleChallenge(Challenge challenge) {
        // 1. 중복 정산 방지
        if (Boolean.TRUE.equals(challenge.getIsSettled())) {
            log.debug("챌린지 ID: {} — 이미 정산 완료된 챌린지입니다.", challenge.getId());
            return;
        }

        // 2. ONGOING 상태인 멤버 목록 조회
        List<ChallengeMember> ongoingMembers = challengeMemberRepository
            .findAllByChallengeIdAndChallengeMemberStatus(
                challenge.getId(), ChallengeMemberStatus.ONGOING);

        if (ongoingMembers.isEmpty()) {
            log.info("챌린지 ID: {} — ONGOING 멤버가 없어 정산을 건너뜁니다.", challenge.getId());
            challenge.markAsSettled();
            return;
        }

        // 3. 성공/실패 판정
        List<ChallengeMember> successMembers = judgeMembers(challenge, ongoingMembers);

        if (!successMembers.isEmpty()) {
            // 4. 성공 멤버 배지 획득(카운트 증가) 처리
            grantCategoryBadges(challenge, successMembers);

            // 5. 포인트 정산
            distributePoints(challenge, successMembers);
        } else {
            // 전원 실패 시 포인트 분배 없음 (collectedPoint 소멸)
            log.info("챌린지 ID: {} — 전원 실패. collectedPoint {} 소멸",
                challenge.getId(), challenge.getCollectedPoint());
        }

        // 5. 정산 완료 표시
        challenge.markAsSettled();
        log.info("챌린지 ID: {} — 정산 완료", challenge.getId());
    }

    /**
     * ONGOING 멤버들의 성공/실패를 판정합니다.
     * 판정 기준: successDays >= totalGoalDay → SUCCESS, 그 외 → FAIL
     *
     * @param challenge      정산 대상 챌린지
     * @param ongoingMembers ONGOING 상태인 챌린지 멤버 목록
     * @return 성공한 챌린지 멤버 목록
     */
    private List<ChallengeMember> judgeMembers(Challenge challenge,
        List<ChallengeMember> ongoingMembers) {

        List<ChallengeMember> successMembers = new ArrayList<>();

        for (ChallengeMember member : ongoingMembers) {
            if (member.getSuccessDays() >= challenge.getTotalGoalDay()) {
                member.updateChallengeMemberStatus(ChallengeMemberStatus.SUCCESS);
                successMembers.add(member);
            } else {
                member.updateChallengeMemberStatus(ChallengeMemberStatus.FAIL);
            }
        }

        log.info("챌린지 ID: {} — 판정 완료 (전체: {}명, 성공: {}명, 실패: {}명)",
            challenge.getId(), ongoingMembers.size(),
            successMembers.size(), ongoingMembers.size() - successMembers.size());

        return successMembers;
    }

    /**
     * 성공 멤버들에게 포인트를 균등 분배합니다.
     * 나머지 포인트(collectedPoint % 성공 멤버 수)는 소멸 처리합니다.
     *
     * @param challenge      정산 대상 챌린지
     * @param successMembers 성공한 챌린지 멤버 목록
     */
    private void distributePoints(Challenge challenge, List<ChallengeMember> successMembers) {
        int collectedPoint = challenge.getCollectedPoint();
        int successCount = successMembers.size();

        // 1인당 분배 포인트 (나머지는 소멸)
        int pointPerMember = collectedPoint / successCount;
        int remainderPoint = collectedPoint % successCount;

        log.info("챌린지 ID: {} — 포인트 분배 (총: {}, 1인당: {}, 소멸: {})",
            challenge.getId(), collectedPoint, pointPerMember, remainderPoint);

        for (ChallengeMember challengeMember : successMembers) {
            // 챌린지 성공 포인트 지급
            pointService.grantChallengePoints(
                challengeMember.getMember(), challenge, pointPerMember,
                PointName.SUCCESS_CHALLENGE);

            // 호스트인 경우 보너스 포인트 추가 지급
            // TODO: 호스트 보너스 정책 미확정 — 추후 논의 안건 상정 필요
            if (Boolean.TRUE.equals(challengeMember.getIsHost()) && HOST_BONUS_POINT > 0) {
                pointService.grantChallengePoints(
                    challengeMember.getMember(), challenge, HOST_BONUS_POINT,
                    PointName.SUCCESS_CHALLENGE_HOST);

                log.info("챌린지 ID: {} — 호스트(멤버 ID: {}) 보너스 포인트 {} 지급",
                    challenge.getId(), challengeMember.getMember().getId(), HOST_BONUS_POINT);
            }
        }
    }

    /**
     * 성공 멤버들에게 카테고리 뱃지 획득(카운트 증가) 처리를 합니다.
     *
     * @param challenge      정산 대상 챌린지
     * @param successMembers 성공한 챌린지 멤버 목록
     */
    private void grantCategoryBadges(Challenge challenge, List<ChallengeMember> successMembers) {
        for (ChallengeMember member : successMembers) {
            badgeService.incrementCategoryBadgeCount(
                member.getMember().getId(),
                challenge.getCategory()
            );
        }
        log.info("챌린지 ID: {} — 성공 멤버 {}명에 대해 카테고리({}) 뱃지 갱신 완료",
            challenge.getId(), successMembers.size(), challenge.getCategory());
    }
}
