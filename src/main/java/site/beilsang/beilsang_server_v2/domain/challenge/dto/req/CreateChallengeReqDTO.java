package site.beilsang.beilsang_server_v2.domain.challenge.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengePeriod;

@Getter
@Schema(description = "챌린지 생성 요청 DTO")
public class CreateChallengeReqDTO {
    @Schema(description = "챌린지 제목", example = "매일 물 8잔 마시기")
    private String title;
    
    @Schema(description = "챌린지 시작 날짜", example = "2024-01-01")
    private LocalDate startDate;
    
    @Schema(description = "챌린지 기간", example = "WEEK")
    private ChallengePeriod period;
    
    @Schema(description = "목표 실천 일수", example = "5")
    private Integer totalGoalDay;
    
    @Schema(description = "카테고리", example = "TUMBLER")
    private Category category;
    
    @Schema(description = "챌린지 세부설명", example = "하루에 물 8잔을 마시는 건강한 습관을 기르는 챌린지입니다.")
    private String details;
    
    @Schema(description = "챌린지 유의사항", example = "[\"물은 생수나 정제수를 마셔주세요\", \"한 번에 많이 마시지 말고 나눠서 마셔주세요\"]")
    private List<String> notes;
    
    @Schema(description = "참여 포인트", example = "100")
    private Integer joinPoint;
}
