package site.beilsang.beilsang_server_v2.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PointName {

    // 챌린지 관련
    JOIN_CHALLENGE("챌린지 참여"),
    SUCCESS_CHALLENGE("챌린지 성공"),
    SUCCESS_CHALLENGE_HOST("챌린지 호스트 성공"),

    // 이벤트 관련
    NEW_MEMBER("신규 회원"),
    ATTENDANCE("출석 보상")
    ;

    private final String description;
}
