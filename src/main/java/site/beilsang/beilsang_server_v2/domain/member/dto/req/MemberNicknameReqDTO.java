package site.beilsang.beilsang_server_v2.domain.member.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원 닉네임 수정 요청 DTO")
public class MemberNicknameReqDTO {

    @NotBlank(message = "닉네임은 필수입니다")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,15}$", message = "닉네임은 2~15자의 한글, 영문, 숫자만 사용할 수 있습니다")
    @Schema(description = "닉네임", example = "환경지킴이")
    private String nickName;
}
