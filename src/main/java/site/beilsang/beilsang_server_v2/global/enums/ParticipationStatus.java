package site.beilsang.beilsang_server_v2.global.enums;

/**
 * 나의 챌린지 참여 상태
 * - ONGOING: 참여중 (ChallengeMemberStatus = ONGOING 또는 NOT_YET)
 * - SUCCESS: 달성 (ChallengeMemberStatus = SUCCESS)
 * - FAIL: 실패 (ChallengeMemberStatus = FAIL)
 */
public enum ParticipationStatus {
    ONGOING,    // 참여중 (진행중 + 시작 전)
    SUCCESS,    // 달성
    FAIL        // 실패
}