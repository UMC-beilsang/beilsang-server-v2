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
    NOT_FOUND_JWT(HttpStatus.UNAUTHORIZED, "O0003", "액세스 토큰이 존재하지 않습니다"),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "O0004", "유효하지 않은 소셜 로그인 제공자입니다"),
    KAKAO_LOGIN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "O0005", "카카오 로그인에 실패했습니다"),
    KAKAO_LOGOUT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "O0006", "카카오 로그아웃에 실패했습니다"),
    KAKAO_UNLINK_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "O0007", "카카오 연결 해제에 실패했습니다"),
    APPLE_LOGOUT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "O0008", "애플 로그아웃에 실패했습니다"),
    APPLE_REVOKE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "O0009", "애플 탈퇴에 실패했습니다"),
    INVALID_APPLE_AUTHORIZATION_CODE(HttpStatus.UNAUTHORIZED, "O0010", "유효하지 않거나 만료된 인증 코드입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "O0011", "유효하지 않은 리프레시 토큰입니다."),
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "O0012", "닉네임은 2~15자의 한글, 영문, 숫자만 사용할 수 있습니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "O0013", "이미 사용 중인 닉네임입니다."),
    NICKNAME_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "O0014", "닉네임 생성에 실패했습니다."),

    // member
    NOT_FOUND_MEMBER(HttpStatus.BAD_REQUEST, "M0001", "존재하지 않은 멤버입니다"),

    // point
    NOT_ENOUGH_POINT(HttpStatus.BAD_REQUEST, "P0001", "포인트가 부족합니다."),

    // feed
    NOT_FOUND_FEED(HttpStatus.BAD_REQUEST, "F0001", "존재하지 않는 피드입니다"),

    // challenge
    INVALID_IMAGE_FILE(HttpStatus.BAD_REQUEST, "C0001", "유효하지 않은 이미지 파일입니다"),
    INVALID_CHALLENGE_PERIOD(HttpStatus.BAD_REQUEST, "C0002", "목표 실천일수가 챌린지 기간을 초과할 수 없습니다"),
    INVALID_START_DATE(HttpStatus.BAD_REQUEST, "C0003", "시작 날짜는 오늘 이후여야 합니다"),
    NOT_FOUND_CHALLENGE(HttpStatus.BAD_REQUEST, "C0004", "존재하지 않는 챌린지입니다"),
    ALREADY_JOINED_CHALLENGE(HttpStatus.BAD_REQUEST, "C0005", "이미 참여한 챌린지입니다"),
    CHALLENGE_ENDED(HttpStatus.BAD_REQUEST, "C0006", "종료된 챌린지입니다"),
    ALREADY_LIKED_CHALLENGE(HttpStatus.BAD_REQUEST, "C0007", "이미 찜한 챌린지입니다"),
    NOT_LIKED_CHALLENGE(HttpStatus.BAD_REQUEST, "C0008", "찜하지 않은 챌린지입니다"),
    NOT_JOINED_CHALLENGE(HttpStatus.BAD_REQUEST, "C0009", "참여하지 않은 챌린지입니다"),
    CHALLENGE_NOT_ENDED(HttpStatus.BAD_REQUEST, "C0010", "완료되지 않은 챌린지입니다"),
    CHALLENGE_NOT_STARTED(HttpStatus.BAD_REQUEST, "C0011", "아직 시작하지 않은 챌린지입니다"),

    //badge
    NOT_FOUND_MEMBER_BADGE(HttpStatus.BAD_REQUEST, "B0001", "보유하지 않은 배지입니다"),
    NOT_ELIGIBLE_BADGE(HttpStatus.BAD_REQUEST, "B0002", "4단계 배지가 아닙니다"),
    NOT_FOUND_BADGE(HttpStatus.BAD_REQUEST, "B0003", "존재하지 않는 배지입니다"),
    
    // report
    ALREADY_REPORTED(HttpStatus.BAD_REQUEST, "R0001", "이미 신고한 피드/챌린지입니다"),
    ALREADY_HIDDEN_FEED(HttpStatus.BAD_REQUEST, "R0002", "이미 숨김 처리된 피드입니다"),
    ALREADY_HIDDEN_CHALLENGE(HttpStatus.BAD_REQUEST, "R0003", "이미 숨김 처리된 챌린지입니다"),
    MISSING_REPORT_DETAIL(HttpStatus.BAD_REQUEST, "R0004", "기타 신고 사유를 입력해주세요");

    private final HttpStatus status; // 커스텀 상태코드
    private final String code; // Http 상태코드
    private final String message;

    public static BaseResponseCode findByCode(String code) {
        return Arrays.stream(BaseResponseCode.values())
            .filter(b -> b.getCode().equals(code))
            .findAny().orElseThrow(() -> new BaseException(BAD_REQUEST));
    }
}
