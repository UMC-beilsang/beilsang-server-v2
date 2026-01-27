package site.beilsang.beilsang_server_v2.domain.challenge.dto.res;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;
import site.beilsang.beilsang_server_v2.global.enums.ChallengePeriod;

@Getter
@Builder
public class ChallengeDetailResDTO {
    private Long challengeId;
    private String title;
    private String description;
    private Category category;
    private LocalDate startDate;
    private LocalDate finishDate;
    private ChallengePeriod period;
    private Integer totalGoalDay;
    private Integer joinPoint;
    private Integer attendeeCount;
    private Integer likeCount;
    private Boolean isLiked; // 현재 사용자가 찜했는지 여부
    private List<String> infoImageUrls;
    private List<String> certImageUrls;
    private List<String> challengeNotes;
    private Boolean isJoinable;
    private ChallengeMemberStatus status;
    private Float progress;
    private Integer usedPoint;
    private Integer earnedPoint;
}