package site.beilsang.beilsang_server_v2.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum BadgeType {

    /**
     * 활동 배지 (3종) - 단계 없음
     */
    CHALLENGE_START,    // 챌린지 1개 참여
    CHALLENGE_CREATE,   // 챌린지 1개 제작
    CHALLENGE_VERIFY,   // 챌린지 인증 피드 1개 인증
}
