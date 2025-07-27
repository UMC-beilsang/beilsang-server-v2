package site.beilsang.beilsang_server_v2.domain.challenge.dto.res;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;
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
    private List<String> infoImageUrls;
    private List<String> certImageUrls;
    private List<String> challengeNotes;
    private Boolean isJoinable;
    private ChallengeStatus status;
    private Float progress;
    private Integer usedPoint;
    private Integer earnedPoint;
}