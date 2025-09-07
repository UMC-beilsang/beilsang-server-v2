package site.beilsang.beilsang_server_v2.domain.feed.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "피드 수정 응답 DTO")
@Builder
@Getter
public class FeedUpdateResDTO {

    @Schema(description = "피드 ID", example = "1")
    private Long feedId;

    @Schema(description = "수정된 피드 후기", example = "수정된 후기입니다!")
    private String review;

    @Schema(description = "피드 이미지 URL")
    private String feedUrl;

    @Schema(description = "업로드 날짜", example = "2024-01-15")
    private LocalDate uploadDate;

    @Schema(description = "수정 시간")
    private LocalDateTime updatedAt;
}