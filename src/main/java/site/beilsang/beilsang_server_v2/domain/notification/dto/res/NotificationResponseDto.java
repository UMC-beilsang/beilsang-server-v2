package site.beilsang.beilsang_server_v2.domain.notification.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    private Long notificationId;
    private String title;
    private String contents;
    private Boolean isRead;
    private LocalDateTime createdAt;

    // iOS에서 어떤 화면으로 랜딩할지 결정하는 타입 ("CHALLENGE" or "COMMON")
    private String notificationType;

    // 챌린지 상세 페이지 랜딩을 위한 ID (챌린지 알림일 경우에만 존재)
    private Long challengeId;
}
