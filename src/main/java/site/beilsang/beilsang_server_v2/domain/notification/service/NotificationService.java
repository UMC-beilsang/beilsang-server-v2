package site.beilsang.beilsang_server_v2.domain.notification.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import site.beilsang.beilsang_server_v2.domain.notification.dto.res.NotificationResponseDto;

public interface NotificationService {
    Page<NotificationResponseDto> getNotifications(Long memberId, Pageable pageable);
    void readNotification(Long memberId, Long notificationId);
}
