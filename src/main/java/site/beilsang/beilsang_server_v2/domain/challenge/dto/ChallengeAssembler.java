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
            CreateChallengeReqDTO request,
            String mainImageUrl,
            String certImageUrl
    ) {
        Challenge challenge = Challenge.builder()
                .category(request.getCategory())
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .finishDate(request.getStartDate().plusDays(request.getPeriod().getDays() - 1))
                .joinPoint(request.getJoinPoint())
                .mainImageUrl(mainImageUrl)
                .certImageUrl(certImageUrl)
                .details(request.getDetails())
                .period(request.getPeriod())
                .category(request.getCategory())
                .totalGoalDay(request.getTotalGoalDay())
                .attendeeCount(1)
                .countLikes(0)
                .collectedPoint(request.getJoinPoint())
                .build();

        return null;
    }
}
