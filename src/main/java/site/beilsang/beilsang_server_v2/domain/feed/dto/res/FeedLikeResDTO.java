package site.beilsang.beilsang_server_v2.domain.feed.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "피드 좋아요 추가/취소 응답 DTO")
@Builder
@Getter
public class FeedLikeResDTO {

    @Schema(description = "피드 ID", example = "1")
    private Long feedId;

    @Schema(description = "현재 좋아요 수", example = "6")
    private Long likeCount;

    @Schema(description = "현재 사용자의 좋아요 여부", example = "true")
    private Boolean isLiked;
}