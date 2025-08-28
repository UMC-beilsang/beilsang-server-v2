package site.beilsang.beilsang_server_v2.domain.challenge.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 챌린지 참여 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinChallengeResDTO {
    
    /**
     * 참여한 챌린지 ID
     */
    private Long challengeId;
    
    /**
     * 참여한 멤버 ID
     */
    private Long memberId;
    
    /**
     * 참여 일시
     */
    private LocalDateTime joinDate;
    
    /**
     * 참여 후 남은 포인트
     */
    private Integer remainingPoint;
}