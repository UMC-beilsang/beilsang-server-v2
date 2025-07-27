package site.beilsang.beilsang_server_v2.domain.challenge.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeListResDTO {
    private Long id;
    private String title;
    private Category category;
    private ChallengeStatus status;
    private int participantCount;
    private int likeCount;
    private String imageUrl;
    private String description;
}