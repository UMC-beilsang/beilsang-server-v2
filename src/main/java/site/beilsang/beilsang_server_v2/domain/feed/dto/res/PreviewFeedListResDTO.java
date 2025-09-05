package site.beilsang.beilsang_server_v2.domain.feed.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Schema(description = "피드 미리보기 DTO List")
@Builder
@Getter
public class PreviewFeedListResDTO {
    @Schema(description = "피드 미리보기 DTO")
    private List<PreviewFeedResDTO> feeds;

    @Schema(description = "다음 페이지 존재 여부")
    private Boolean hasNext;
}
