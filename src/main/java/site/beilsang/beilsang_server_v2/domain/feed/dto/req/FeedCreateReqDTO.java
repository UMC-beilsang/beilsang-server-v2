package site.beilsang.beilsang_server_v2.domain.feed.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "피드 작성 요청 DTO")
@Getter
@Setter
public class FeedCreateReqDTO {

    @Schema(description = "챌린지 멤버 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "챌린지 멤버 ID는 필수입니다.")
    private Long challengeMemberId;

    @Schema(description = "피드 후기", example = "오늘도 열심히 운동했어요!", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "피드 후기는 필수입니다.")
    private String review;
}
