package site.beilsang.beilsang_server_v2.domain.challenge.dto;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;

@Slf4j
@Component
public class ChallengeAssembler {

    public static Challenge toEntity(
            CreateChallengeReqDTO request,
            String mainImageUrl,
            String certImageUrl
    ) {
        return Challenge.builder()
                .category(request.getCategory())
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .finishDate(request.getStartDate().plusDays(request.getPeriod().getDays() - 1))
                .joinPoint(request.getJoinPoint())
                .mainImageUrl(mainImageUrl)
                .certImageUrl(certImageUrl)
                .details(request.getDetails())
                .period(request.getPeriod())
                .totalGoalDay(request.getTotalGoalDay())
                .attendeeCount(1)
                .countLikes(0)
                .collectedPoint(request.getJoinPoint())
                .build();
    }

    public static ChallengeResDTO toChallengeResDTO(Challenge challenge) {
        return ChallengeResDTO.builder()
                .challengeId(challenge.getId())
                .category(challenge.getCategory())
                .title(challenge.getTitle())
                .startDate(challenge.getStartDate())
                .finishDate(challenge.getFinishDate())
                .joinPoint(challenge.getJoinPoint())
                .mainImageUrl(challenge.getMainImageUrl())
                .certImageUrl(challenge.getCertImageUrl())
                .details(challenge.getDetails())
                .period(challenge.getPeriod())
                .totalGoalDay(challenge.getTotalGoalDay())
                .attendeeCount(challenge.getAttendeeCount())
                .countLikes(challenge.getCountLikes())
                .collectedPoint(challenge.getCollectedPoint())
                .build();
    }
}
