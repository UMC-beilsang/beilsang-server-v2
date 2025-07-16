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
public class ChallengeListResponseDTO {
    private Long id;
    private String title;
    private Category category;
    private ChallengeMemberStatus status;
    private int participantCount;
    private int likeCount;
    private String imageUrl;
    private String description;
}