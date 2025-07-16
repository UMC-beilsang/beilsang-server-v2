package site.beilsang.beilsang_server_v2.domain.challenge.dto;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeListResponseDTO;

@Slf4j
@Component
public class ChallengeAssembler {

        public static Challenge toEntity(CreateChallengeReqDTO request) {
                return Challenge.builder()
                                .category(request.getCategory())
                                .title(request.getTitle())
                                .startDate(request.getStartDate())
                                .finishDate(request.getStartDate().plusDays(request.getPeriod().getDays() - 1))
                                .joinPoint(request.getJoinPoint())
                                .details(request.getDetails())
                                .period(request.getPeriod())
                                .totalGoalDay(request.getTotalGoalDay())
                                .attendeeCount(1)
                                .countLikes(0)
                                .collectedPoint(request.getJoinPoint())
                                .build();
        }

        public static ChallengeResDTO toChallengeResDTO(Challenge challenge) {
                // 정보 이미지 URL 리스트 생성
                List<String> infoImageUrls = challenge.getInfoImages().stream()
                                .sorted((a, b) -> a.getImageOrder().compareTo(b.getImageOrder()))
                                .map(image -> image.getImageUrl())
                                .toList();

                // 인증 이미지 URL 리스트 생성
                List<String> certImageUrls = challenge.getCertImages().stream()
                                .sorted((a, b) -> a.getImageOrder().compareTo(b.getImageOrder()))
                                .map(image -> image.getImageUrl())
                                .toList();

                return ChallengeResDTO.builder()
                                .challengeId(challenge.getId())
                                .category(challenge.getCategory())
                                .title(challenge.getTitle())
                                .startDate(challenge.getStartDate())
                                .finishDate(challenge.getFinishDate())
                                .joinPoint(challenge.getJoinPoint())
                                .infoImageUrls(infoImageUrls)
                                .certImageUrls(certImageUrls)
                                .details(challenge.getDetails())
                                .period(challenge.getPeriod())
                                .totalGoalDay(challenge.getTotalGoalDay())
                                .attendeeCount(challenge.getAttendeeCount())
                                .countLikes(challenge.getCountLikes())
                                .collectedPoint(challenge.getCollectedPoint())
                                .build();
        }

        public static ChallengeListResponseDTO toChallengeListResponseDTO(Challenge challenge) {
                String imageUrl = null;
                if (!challenge.getInfoImages().isEmpty()) {
                        imageUrl = challenge.getInfoImages().get(0).getImageUrl();
                }
                return ChallengeListResponseDTO.builder()
                                .id(challenge.getId())
                                .title(challenge.getTitle())
                                .category(challenge.getCategory())
                                .status(null) // TODO: ChallengeMemberStatus 추가
                                .participantCount(challenge.getAttendeeCount())
                                .likeCount(challenge.getCountLikes())
                                .imageUrl(imageUrl)
                                .description(challenge.getDetails())
                                .build();
        }
}
