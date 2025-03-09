package site.beilsang.beilsang_server_v2.domain.challenge.dto.res;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengePeriod;

@Builder
public class ChallengeResDTO {
    private Long challengeId;
    private Category category;
    private String title;
    private LocalDate startDate;
    private LocalDate finishDate;
    private Integer joinPoint;
    private String mainImageUrl;
    private String certImageUrl;
    private String details;
    private List<String> challengeNotes;
    private ChallengePeriod period;
    private Integer totalGoalDay;
    private Integer attendeeCount;
    private Integer countLikes;
    private Integer collectedPoint;
}