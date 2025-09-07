package site.beilsang.beilsang_server_v2.domain.feed.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import site.beilsang.beilsang_server_v2.global.enums.Category;

@Schema(description = "피드 목록 조회 요청 DTO")
@Getter
@Setter
public class FeedListReqDTO {

    @Schema(description = "카테고리", example = "EXERCISE")
    private Category category;

    @Schema(description = "페이지 번호", example = "0")
    private int page = 0;

    @Schema(description = "페이지 크기", example = "20")
    private int size = 20;
}