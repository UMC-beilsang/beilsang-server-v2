package site.beilsang.beilsang_server_v2.domain.challenge.dto;

import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.global.enums.Category;

@Slf4j
@Component
public class ChallengeAssembler {

    public static Challenge toEntity(
            CreateChallengeReqDTO request, LocalDate finishDate, String mainImageUrl, String certImageUrl
    ) {
        Challenge challenge = Challenge.builder()
                .category(request.getCategory())
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .finishDate(finishDate)
                .period(request.getPeriod())
                .totalGoalDay(request.getTotalGoalDay())
                .category(request.getCategory())
                .mainImageUrl(mainImageUrl)
                .certImageUrl(certImageUrl)
                .build();

        return null;
    }
}
