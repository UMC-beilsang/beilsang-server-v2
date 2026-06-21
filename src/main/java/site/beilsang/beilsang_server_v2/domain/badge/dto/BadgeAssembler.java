package site.beilsang.beilsang_server_v2.domain.badge.dto;

import site.beilsang.beilsang_server_v2.domain.badge.dto.res.BadgeResDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.BadgeMember;

import java.util.List;

public class BadgeAssembler {
    public static BadgeResDTO toBadgeResDTO(BadgeMember memberBadge) {
        return BadgeResDTO.builder()
            .badgeMemberId(memberBadge.getId())
            .badgeId(memberBadge.getBadge().getId())
            .badgeType(memberBadge.getBadge().getBadgeType())
            .category(memberBadge.getBadge().getCategory())
            .currentStep(memberBadge.getStep())
            .acquiredAt(memberBadge.getAcquiredAt())
            .build();
    }
    public static List<BadgeResDTO> toBadgeResDTOList(List<BadgeMember> badges) {
        return badges.stream().map(BadgeAssembler::toBadgeResDTO).toList();
    }
}
