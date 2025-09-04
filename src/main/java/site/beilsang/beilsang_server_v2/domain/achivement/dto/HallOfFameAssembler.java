package site.beilsang.beilsang_server_v2.domain.achivement.dto;

import org.springframework.stereotype.Component;
import site.beilsang.beilsang_server_v2.domain.achivement.dto.res.HallOfFameResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.ChallengeInfoImage;

import java.util.List;

@Component
public class HallOfFameAssembler {
    public static HallOfFameResDTO toHallfOfFameResDTO(Challenge challenge, int rank) {
        List<String> infoImageUrls = challenge.getInfoImages().stream()
            .map(ChallengeInfoImage::getImageUrl)
            .toList();

        return HallOfFameResDTO.builder()
            .challengeId(challenge.getId())
            .title(challenge.getTitle())
            .category(challenge.getCategory())
            .categoryName(challenge.getCategory().getKorName())
            .startDate(challenge.getStartDate())
            .finishDate(challenge.getFinishDate())
            .likeCount(challenge.getCountLikes())
            .attendeeCount(challenge.getAttendeeCount())
            .rank(rank)
            .infoImageUrls(infoImageUrls)
            .build();
    }
}
