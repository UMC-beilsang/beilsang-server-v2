package site.beilsang.beilsang_server_v2.domain.challenge.dto.req;

import lombok.Getter;
import lombok.Setter;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeSortField;
import site.beilsang.beilsang_server_v2.global.enums.SortDirection;

@Getter
@Setter
public class ChallengeListReqDTO {
    private Integer page = 0;
    private Integer size = 10;
    private ChallengeSortField sortField = ChallengeSortField.FINISH_DATE;
    private SortDirection sortDirection = SortDirection.DESC;
    private Category category;
    private ChallengeStatus status;
    private String keyword;
    private Boolean isFinished;
    private Boolean isJoined;
    private Long memberId;
}