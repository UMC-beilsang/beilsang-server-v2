package site.beilsang.beilsang_server_v2.domain.feed.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "피드 작성 응답 DTO")
@Builder
@Getter
public class FeedCreateResDTO {

    @Schema(description = "피드 ID", example = "1")
    private Long feedId;

    @Schema(description = "챌린지 제목", example = "30일 운동 챌린지")
    private String challengeTitle;

    @Schema(description = "피드 후기", example = "오늘도 열심히 운동했어요!")
    private String review;

    @Schema(description = "피드 이미지 URL")
    private String feedUrl;

    @Schema(description = "업로드 날짜", example = "2024-01-15")
    private LocalDate uploadDate;

    @Schema(description = "생성 시간")
    private LocalDateTime createdAt;
}