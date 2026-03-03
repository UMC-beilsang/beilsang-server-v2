package site.beilsang.beilsang_server_v2.domain.member.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "약관 동의 요청 DTO")
public class TermsAgreementReqDTO {

    @NotNull(message = "약관 동의 여부는 필수입니다")
    @Schema(description = "약관 동의 여부", example = "true")
    private Boolean agreed;
}