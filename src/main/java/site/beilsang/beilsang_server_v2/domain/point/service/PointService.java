package site.beilsang.beilsang_server_v2.domain.point.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;
import site.beilsang.beilsang_server_v2.domain.point.repository.PointLogRepository;
import site.beilsang.beilsang_server_v2.global.enums.PointStatus;

@Service
@RequiredArgsConstructor
@Transactional
public class PointService {

    private final PointLogRepository pointLogRepository;

    public void expirePointsIfNeeded(List<PointLog> pointLogs) {
        LocalDateTime now = LocalDateTime.now();
        boolean hasChanges = false;

        for (PointLog pointLog : pointLogs) {
            if (pointLog.getExpirationDate() != null &&
                pointLog.getExpirationDate().isBefore(now) &&
                pointLog.getStatus() != PointStatus.EXPIRE) {
                pointLog.setStatus(PointStatus.EXPIRE);
                hasChanges = true;
            }
        }

        if (hasChanges) {
            pointLogRepository.saveAll(pointLogs);
        }
    }

    public int calculateValidPoints(Long memberId) {
        List<PointLog> pointLogs = pointLogRepository.findAllByMemberIdAndStatusNot(memberId,
            PointStatus.EXPIRE);
        expirePointsIfNeeded(pointLogs);

        return pointLogs.stream()
            .filter(p -> p.getStatus() != PointStatus.EXPIRE)
            .mapToInt(PointLog::getValue)
            .sum();
    }
}
