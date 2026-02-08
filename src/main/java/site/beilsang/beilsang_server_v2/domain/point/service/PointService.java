package site.beilsang.beilsang_server_v2.domain.point.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;
import site.beilsang.beilsang_server_v2.domain.point.repository.PointLogRepository;
import site.beilsang.beilsang_server_v2.global.enums.PointName;
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
            .mapToInt(PointLog::getPoints)
            .sum();
    }

    /**
     * 회원에게 포인트를 지급하고 포인트 로그를 생성합니다.
     *
     * @param member    포인트를 지급받을 회원
     * @param points    지급할 포인트 금액
     * @param pointName 포인트 지급 사유
     */
    public void grantPoints(Member member, int points, PointName pointName) {
        // 포인트 로그 생성
        PointLog pointLog = PointLog.builder()
            .pointName(pointName)
            .status(PointStatus.EARN)
            .points(points)
            .expirationDate(LocalDateTime.now().plusYears(1))       // 포인트 만료 기한 설정
            .member(member)
            .build();

        pointLogRepository.save(pointLog);

        // 회원 포인트 잔액 업데이트
        member.addPoint(points);
    }

    /**
     * 챌린지 정산 포인트를 지급하고 포인트 로그를 생성합니다.
     * 기존 grantPoints와 달리 PointLog에 챌린지 정보를 연결하여 정산 내역 추적이 가능합니다.
     *
     * @param member    포인트를 지급받을 회원
     * @param challenge 정산 대상 챌린지
     * @param points    지급할 포인트 금액
     * @param pointName 포인트 지급 사유 (SUCCESS_CHALLENGE 또는 SUCCESS_CHALLENGE_HOST)
     */
    public void grantChallengePoints(Member member, Challenge challenge, int points,
        PointName pointName) {
        // 포인트 로그 생성 (챌린지 정보 포함)
        PointLog pointLog = PointLog.builder()
            .pointName(pointName)
            .status(PointStatus.EARN)
            .points(points)
            .expirationDate(LocalDateTime.now().plusYears(1))
            .member(member)
            .challenge(challenge)
            .build();

        pointLogRepository.save(pointLog);

        // 회원 포인트 잔액 업데이트
        member.addPoint(points);
    }
}
