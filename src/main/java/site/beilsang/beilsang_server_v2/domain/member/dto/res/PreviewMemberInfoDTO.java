package site.beilsang.beilsang_server_v2.domain.member.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "멤버 정보 DTO")
@Builder
@Getter
public class PreviewMemberInfoDTO {

    @Schema(description = "작성자 ID", example = "1")
    private Long memberId;

    @Schema(description = "작성자 닉네임", example = "사용자1")
    private String memberNickname;

    @Schema(description = "작성자 프로필 이미지 URL")
    private String memberProfileImage;
}
