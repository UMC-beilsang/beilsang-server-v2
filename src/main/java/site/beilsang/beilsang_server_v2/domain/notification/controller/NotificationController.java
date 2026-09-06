package site.beilsang.beilsang_server_v2.domain.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.beilsang.beilsang_server_v2.domain.notification.dto.res.NotificationResponseDto;
import site.beilsang.beilsang_server_v2.domain.notification.service.FCMService;
import site.beilsang.beilsang_server_v2.domain.notification.service.NotificationService;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;

import java.util.Map;


@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
@Tag(name = "Notification", description = "알림 관리 API")
public class NotificationController {
    private final NotificationService appNotificationService;
    private final FCMService fcmService;

    @Operation(
        summary = "알림 목록 조회",
        description = "사용자의 알림 목록을 페이징하여 최신순으로 조회합니다.",
        parameters = {
            @Parameter(in = ParameterIn.QUERY, name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(in = ParameterIn.QUERY, name = "size", description = "한 페이지당 알림 개수", example = "20"),
            @Parameter(in = ParameterIn.QUERY, name = "sort", description = "정렬 기준 - 필드명, acs/desc로 설정 (예: createdAt,DESC, isRead,asc)", example = "createdAt,DESC")
        }
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public BaseResponse<Page<NotificationResponseDto>> getNotifications(
        Authentication authentication,
        @Parameter(hidden = true) // 파라미터 중복 방지를 위해 Pageable 자체는 숨김 처리
        @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
        Pageable pageable
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(
            appNotificationService.getNotifications(memberId, pageable)
        );
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음")
    })
    @PatchMapping("/{notificationId}/read")
    public BaseResponse<Void> readNotification(
        Authentication authentication,
        @Parameter(description = "읽음 처리할 알림의 ID") @PathVariable("notificationId") Long notificationId
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        appNotificationService.readNotification(memberId, notificationId);

        // BaseResponse의 구조에 따라 데이터가 없는 경우 null 또는 빈 객체를 반환하도록 맞춰주세요.
        return new BaseResponse<>();
    }

    @PostMapping("/test-push")
    @Operation(summary = "iOS 푸시 알림 직접 테스트 API", description = "입력한 deviceToken으로 FCM 푸시 알림을 즉시 발송합니다.")
    public BaseResponse<String> testPushNotification(
        @RequestParam String deviceToken,
        @RequestParam(defaultValue = "1") Long challengeId,
        @RequestParam(defaultValue = "RECOMMEND_CHALLENGE") String notificationType,
        @RequestParam(defaultValue = "테스트 알림 제목") String title,
        @RequestParam(defaultValue = "테스트 알림 내용입니다.") String body
    ) {
        fcmService.sendToToken(
            deviceToken,
            title,
            body,
            Map.of(
                "challengeId", String.valueOf(challengeId),
                "notificationType", notificationType
            )
        );
        return new BaseResponse<>("테스트 푸시 알림 요청 완료");
    }
}
