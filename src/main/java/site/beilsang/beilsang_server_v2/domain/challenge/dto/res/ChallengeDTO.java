package site.beilsang.beilsang_server_v2.domain.challenge.dto.res;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengePeriod;

@Builder
public class ChallengeDTO {
    private Long challengeId;
    private Integer attendeeCount;
    private String hostName;
    private LocalDate createdDate;
    private String imageUrl;
    private String certImageUrl;
    private String title;
    private LocalDate startDate;
    private DayOfWeek dayOfWeek;
    private Category category;
    private String details;
    private Integer joinPoint;
    private Integer dDay;
    private List<String> challengeNotes;
    private Integer likes;
    private boolean like;
    private ChallengePeriod period;
    private Integer totalGoalDay;
    private Float achieveRate;
}