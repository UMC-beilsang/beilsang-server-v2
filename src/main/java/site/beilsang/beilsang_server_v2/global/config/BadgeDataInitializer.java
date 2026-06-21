package site.beilsang.beilsang_server_v2.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import site.beilsang.beilsang_server_v2.domain.badge.entity.Badge;
import site.beilsang.beilsang_server_v2.domain.badge.repository.BadgeRepository;
import site.beilsang.beilsang_server_v2.global.enums.BadgeType;
import site.beilsang.beilsang_server_v2.global.enums.Category;

@Component
@RequiredArgsConstructor
public class BadgeDataInitializer implements ApplicationRunner {

    private final BadgeRepository badgeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // 이미 Badge 데이터가 존재한다면 서버가 재시작되지 않았기 때문에 종료
        if (badgeRepository.count() != 0) {
            return;
        }

        // 1. 활동 배지 초기화 (CHALLENGE_START, CREATE, VERIFY)
        for (BadgeType badgeType : BadgeType.values()) {
            // 카테고리용 BadgeType(예: CATEGORY)이 Enum에 있다면 제외하고 처리
            if (badgeType == BadgeType.CATEGORY) continue;

            if (!badgeRepository.existsByBadgeType(badgeType)) {
                badgeRepository.save(Badge.builder()
                    .badgeType(badgeType)
                    .category(null) // 활동 배지는 카테고리가 없음
                    .build());
            }
        }

        // 2. 카테고리 배지 9종 초기화
        // Category Enum(예: 건강, 운동, 공부 등 9개)을 순회하며 생성
        for (Category category : Category.values()) {
            if (!badgeRepository.existsByCategory(category)) {
                badgeRepository.save(Badge.builder()
                    .badgeType(BadgeType.CATEGORY) // 타입을 CATEGORY로 통일 (Enum에 CATEGORY 추가 필요)
                    .category(category)            // 각각의 카테고리 할당
                    .build());
            }
        }
    }
}
