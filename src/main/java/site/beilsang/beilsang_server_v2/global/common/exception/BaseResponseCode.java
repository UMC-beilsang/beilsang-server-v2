package site.beilsang.beilsang_server_v2.global.common.exception;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

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
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GL005", "서버 내부 오류입니다."),

    //oauth
    EXPIRED_JWT(HttpStatus.UNAUTHORIZED, "O0001", "토큰이 만료되었습니다"),
    INVALID_JWT(HttpStatus.UNAUTHORIZED, "O0002", "유효하지 않은 토큰입니다"),
    NOT_FOUND_JWT(HttpStatus.UNAUTHORIZED, "O0003", "토큰이 존재하지 않습니다"),
    KAKAO_LOGOUT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "O0004", "카카오 로그아웃에 실패했습니다"),
    KAKAO_UNLINK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "O0005", "카카오 연결 해제에 실패했습니다"),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "O0006", "유효하지 않은 소셜 로그인 제공자입니다"),


    // member
    NOT_FOUND_MEMBER(HttpStatus.BAD_REQUEST, "M0001", "존재하지 않은 멤버입니다"),

    // point
    NOT_ENOUGH_POINT(HttpStatus.BAD_REQUEST, "P0001", "포인트가 부족합니다."),

    // challenge
    INVALID_IMAGE_FILE(HttpStatus.BAD_REQUEST, "C0001", "유효하지 않은 이미지 파일입니다"),
    INVALID_CHALLENGE_PERIOD(HttpStatus.BAD_REQUEST, "C0002", "목표 실천일수가 챌린지 기간을 초과할 수 없습니다"),
    INVALID_START_DATE(HttpStatus.BAD_REQUEST, "C0003", "시작 날짜는 오늘 이후여야 합니다"),
    NOT_FOUND_CHALLENGE(HttpStatus.BAD_REQUEST, "C0004", "존재하지 않는 챌린지입니다"),
    ALREADY_JOINED_CHALLENGE(HttpStatus.BAD_REQUEST, "C0005", "이미 참여한 챌린지입니다"),
    CHALLENGE_ENDED(HttpStatus.BAD_REQUEST, "C0006", "종료된 챌린지입니다");

    private final HttpStatus status; // 커스텀 상태코드
    private final String code; // Http 상태코드
    private final String message;

    public static BaseResponseCode findByCode(String code) {
        return Arrays.stream(BaseResponseCode.values())
            .filter(b -> b.getCode().equals(code))
            .findAny().orElseThrow(() -> new BaseException(BAD_REQUEST));
    }
}
