package site.beilsang.beilsang_server_v2.domain.notification.dto;

import site.beilsang.beilsang_server_v2.domain.notification.dto.res.NotificationResponseDto;
import site.beilsang.beilsang_server_v2.domain.notification.entity.AppNotification;
import site.beilsang.beilsang_server_v2.domain.notification.entity.ChallengeNotification;

public class NotificationAssembler {

    public static NotificationResponseDto toDto(AppNotification notification) {
        Long challengeId = null;
        String notificationType = "COMMON";

        // 다형성을 이용해 타입 체크 및 challengeId 추출
        if (notification instanceof ChallengeNotification challengeNotification) {
            challengeId = challengeNotification.getChallengeId();
            notificationType = "CHALLENGE";
        }

        return NotificationResponseDto.builder()
            .notificationId(notification.getId())
            .title(notification.getTitle())
            .contents(notification.getContents())
            .isRead(notification.getIsRead())
            .createdAt(notification.getCreatedAt())
            .notificationType(notificationType)
            .challengeId(challengeId)
            .build();
    }
}
