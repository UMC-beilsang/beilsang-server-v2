package site.beilsang.beilsang_server_v2.domain.challenge.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeListResDTO {
    private Long id;
    private String title;
    private Category category;
    private ChallengeMemberStatus status;
    private int participantCount;
    private int likeCount;
    private String imageUrl;
    private String description;

    /**
     * 나의 챌린지 달성률 (0.0 ~ 1.0)
     * - 나의 챌린지 조회에서만 값이 채워지고, 그 외 목록 조회에서는 null
     */
    private Float progress;
}
