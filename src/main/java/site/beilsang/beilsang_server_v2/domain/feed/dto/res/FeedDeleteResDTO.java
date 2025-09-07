package site.beilsang.beilsang_server_v2.domain.feed.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "피드 삭제 응답 DTO")
@Builder
@Getter
public class FeedDeleteResDTO {

    @Schema(description = "삭제된 피드 ID", example = "1")
    private Long feedId;

    @Schema(description = "삭제 시간")
    private LocalDateTime deletedAt;
}