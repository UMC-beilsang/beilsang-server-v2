package site.beilsang.beilsang_server_v2.domain.feed.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.PreviewMemberInfoDTO;

@Schema(description = "피드 상세 조회 응답 DTO")
@Builder
@Getter
public class FeedDetailResDTO {

    @Schema(description = "피드 ID", example = "1")
    private Long feedId;

    @Schema(description = "작성자 정보")
    private PreviewMemberInfoDTO memberInfo;

    @Schema(description = "챌린지 ID", example = "1")
    private Long challengeId;

    @Schema(description = "챌린지 제목", example = "30일 운동 챌린지")
    private String challengeTitle;

    @Schema(description = "챌린지 카테고리", example = "EXERCISE")
    private String challengeCategory;

    @Schema(description = "피드 후기", example = "오늘도 열심히 운동했어요!")
    private String review;

    @Schema(description = "피드 이미지 URL")
    private String feedUrl;

    @Schema(description = "업로드 날짜", example = "2024-01-15")
    private LocalDate uploadDate;

    @Schema(description = "좋아요 수", example = "5")
    private Long likeCount;

    @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
    private Boolean isLiked;

    @Schema(description = "내가 작성한 피드 여부", example = "false")
    private Boolean isMyFeed;

    @Schema(description = "생성 시간")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간")
    private LocalDateTime updatedAt;
}
