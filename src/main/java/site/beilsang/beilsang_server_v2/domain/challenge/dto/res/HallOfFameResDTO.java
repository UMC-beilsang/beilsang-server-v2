package site.beilsang.beilsang_server_v2.domain.challenge.dto.res;

import lombok.Builder;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import java.time.LocalDate;
import java.util.List;
@Builder
@Getter
public class HallOfFameResDTO {
    private Long challengeId;
    private String title;
    private Category category;
    private String categoryName;
    private LocalDate startDate;
    private LocalDate finishDate;
    private Integer likeCount;
    private Integer attendeeCount;
    private Integer rank;
    private List<String> infoImageUrls;
}
