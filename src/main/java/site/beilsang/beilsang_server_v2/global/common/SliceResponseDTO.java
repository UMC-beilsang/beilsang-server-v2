package site.beilsang.beilsang_server_v2.global.common;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "슬라이스 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SliceResponseDTO<T> {

    @Schema(description = "실제 데이터 리스트")
    private List<T> content;

    @Schema(description = "현재 슬라이스 번호 (0부터 시작)", example = "0")
    private int number;

    @Schema(description = "요청한 슬라이스 크기", example = "20")
    private int size;

    @Schema(description = "현재 슬라이스의 실제 요소 수", example = "15")
    private int numberOfElements;

    @Schema(description = "다음 슬라이스 존재 여부", example = "true")
    private boolean hasNext;
}
