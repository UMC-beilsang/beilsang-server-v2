package site.beilsang.beilsang_server_v2.domain.member.dto.res;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CheckEnrolledResDTO {

    private Boolean isEnrolled;
    private List<Long> enrolledChallengeIds;
}
