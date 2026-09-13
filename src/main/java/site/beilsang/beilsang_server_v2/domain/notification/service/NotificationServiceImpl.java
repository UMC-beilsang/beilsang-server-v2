package site.beilsang.beilsang_server_v2.domain.notification.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.beilsang.beilsang_server_v2.domain.notification.dto.NotificationAssembler;
import site.beilsang.beilsang_server_v2.domain.notification.dto.res.NotificationResponseDto;
import site.beilsang.beilsang_server_v2.domain.notification.entity.AppNotification;
import site.beilsang.beilsang_server_v2.domain.notification.repository.NotificationRepository;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;


import static site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode.NOT_FOUND_NOTIFICATION;
import static site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode.NO_PERMISSION_NOTIFICATION;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // 유저의 전체 알림 목록 조회 (최신순)
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getNotifications(Long memberId, Pageable pageable) {
        Page<AppNotification> notifications = notificationRepository
            .findAllByMemberIdOrderByCreatedAtDesc(memberId, pageable);

        return notifications.map(NotificationAssembler::toDto);
    }

    // 특정 알림 단건 읽음 처리
    @Override
    @Transactional
    public void readNotification(Long memberId, Long notificationId) {
        AppNotification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new BaseException(NOT_FOUND_NOTIFICATION));

        // 본인 알림인지 권한 체크
        if (!notification.getMember().getId().equals(memberId)) {
            throw new BaseException(NO_PERMISSION_NOTIFICATION);
        }

         notification.read();
    }
}
