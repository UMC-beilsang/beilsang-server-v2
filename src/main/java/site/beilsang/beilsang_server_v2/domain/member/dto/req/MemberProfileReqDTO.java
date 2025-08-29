package site.beilsang.beilsang_server_v2.domain.member.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원 프로필 수정 요청 DTO")
public class MemberProfileReqDTO {

    @Schema(description = "닉네임", example = "환경지킴이")
    private String nickName;

    @Schema(description = "생년월일", example = "1990-01-01")
    private String birth;

    @Schema(description = "성별", example = "MALE")
    private String gender;

    @Schema(description = "주소", example = "서울시 강남구")
    private String address;

    @Schema(description = "환경보호 다짐", example = "매일 텀블러 사용하여 일회용컵 안쓰기")
    private String resolution;
}
