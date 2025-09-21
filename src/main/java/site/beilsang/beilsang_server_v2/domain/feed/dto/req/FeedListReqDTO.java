package site.beilsang.beilsang_server_v2.domain.feed.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    private int page = 0;

    @Schema(description = "페이지 크기", example = "10")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 10, message = "페이지 크기는 10 이하여야 합니다.")
    private int size = 10;
}
