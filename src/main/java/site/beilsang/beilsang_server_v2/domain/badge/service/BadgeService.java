package site.beilsang.beilsang_server_v2.domain.badge.service;

import site.beilsang.beilsang_server_v2.domain.badge.dto.res.BadgeResDTO;
import site.beilsang.beilsang_server_v2.domain.badge.dto.res.RepresentativeBadgeResDTO;
import site.beilsang.beilsang_server_v2.global.enums.BadgeType;
import site.beilsang.beilsang_server_v2.global.enums.Category;

import java.util.List;

public interface BadgeService {

    /** 내 배지 전체 목록 조회 */
    List<BadgeResDTO> getMyBadgeList(Long memberId);

    /** 대표 배지 조회 (미설정 시 null 반환) */
    BadgeResDTO getRepresentativeBadge(Long memberId);

    /** 대표 배지 설정 또는 해제 (requestDTO.memberBadgeId == null 이면 해제) */
    void updateRepresentativeBadge(Long memberId, RepresentativeBadgeResDTO requestDTO);

    /**
     * 활동 배지 최초 1회 부여
     * 이미 해당 BadgeType의 MemberBadge가 존재하면 아무것도 하지 않음
     * ChallengeService, FeedService에서 호출
     *
     * @param memberId  배지를 받을 멤버 ID
     * @param badgeType CHALLENGE_START | CHALLENGE_CREATE | CHALLENGE_VERIFY
     */
    void grantActivityBadgeIfFirst(Long memberId, BadgeType badgeType);


    /** 챌린지 정산(성공) 시 카테고리 뱃지 성공 횟수 +1 */
    void incrementCategoryBadgeCount(Long memberId, Category category);
}
