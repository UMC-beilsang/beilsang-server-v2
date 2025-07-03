package site.beilsang.beilsang_server_v2.domain.challenge.dto.req;

import lombok.Getter;
import lombok.Setter;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;

@Getter
@Setter
public class ChallengeListRequestDTO {
    private Integer page = 0;
    private Integer size = 10;
    private String sort; // 예: "attendeeCount,desc"
    private Category category;
    private ChallengeStatus status;
    private String keyword;
    private Boolean isFinished;
    private Boolean isJoined;
    private Long memberId;
}