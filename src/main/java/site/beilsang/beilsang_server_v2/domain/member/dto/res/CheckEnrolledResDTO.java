package site.beilsang.beilsang_server_v2.domain.member.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CheckEnrolledResDTO {
    private Boolean isEnrolled;
    private List<Long> enrolledChallengeIds;
}
