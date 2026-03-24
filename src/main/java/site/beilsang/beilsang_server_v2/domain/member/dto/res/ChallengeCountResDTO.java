package site.beilsang.beilsang_server_v2.domain.member.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChallengeCountResDTO {

    Long challenges;
    Long successChallenge;
    Long failedChallenges;
}