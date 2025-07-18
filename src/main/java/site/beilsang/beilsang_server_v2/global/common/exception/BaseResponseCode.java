package site.beilsang.beilsang_server_v2.global.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum BaseResponseCode {

    // success
    SUCCESS(HttpStatus.OK, "S0001", "요청에 성공했습니다"),

    // global
    NO_PERMISSION(HttpStatus.FORBIDDEN, "GL001", "권한이 없습니다."),
    NULL_REQUEST_PARAM(HttpStatus.BAD_REQUEST, "GL002", "쿼리 파라미터가 없습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "GL003", "잘못된 요청입니다."),
    CONTENT_NULL(HttpStatus.BAD_REQUEST, "GL004", "내용을 입력해 주세요."),

    // member
    NOT_FOUND_MEMBER(HttpStatus.BAD_REQUEST, "M0001", "존재하지 않은 멤버입니다"),

    // point
    NOT_ENOUGH_POINT(HttpStatus.BAD_REQUEST, "P0001", "포인트가 부족합니다."),

    // challenge
    INVALID_IMAGE_FILE(HttpStatus.BAD_REQUEST, "C0001", "유효하지 않은 이미지 파일입니다"),
    INVALID_CHALLENGE_PERIOD(HttpStatus.BAD_REQUEST, "C0002", "목표 실천일수가 챌린지 기간을 초과할 수 없습니다"),
    INVALID_START_DATE(HttpStatus.BAD_REQUEST, "C0003", "시작 날짜는 오늘 이후여야 합니다"),
    NOT_FOUND_CHALLENGE(HttpStatus.BAD_REQUEST, "C0004", "존재하지 않는 챌린지입니다");

    private final HttpStatus status; // 커스텀 상태코드
    private final String code; // Http 상태코드
    private final String message;

    public static BaseResponseCode findByCode(String code) {
        return Arrays.stream(BaseResponseCode.values())
                .filter(b -> b.getCode().equals(code))
                .findAny().orElseThrow(() -> new BaseException(BAD_REQUEST));
    }
}
