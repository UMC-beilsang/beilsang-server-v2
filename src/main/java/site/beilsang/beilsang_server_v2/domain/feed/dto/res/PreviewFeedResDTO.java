package site.beilsang.beilsang_server_v2.domain.feed.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "피드 미리보기 응답 DTO")
@Builder
@Getter
public class PreviewFeedResDTO {

    @Schema(description = "피드 ID", example = "1")
    private Long feedId;
    
    @Schema(description = "피드 이미지 URL")
    private String feedUrl;
    
    @Schema(description = "챌린지 시작일로부터 경과일", example = "15")
    private Long day;
}
