package site.beilsang.beilsang_server_v2.domain.feed.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Schema(description = "피드 수정 요청 DTO")
@Getter
@Setter
public class FeedUpdateReqDTO {

    @Schema(description = "피드 후기", example = "수정된 후기입니다!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "피드 후기는 필수입니다.")
    private String review;

    @Schema(description = "업로드 날짜", example = "2024-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "업로드 날짜는 필수입니다.")
    private LocalDate uploadDate;
}