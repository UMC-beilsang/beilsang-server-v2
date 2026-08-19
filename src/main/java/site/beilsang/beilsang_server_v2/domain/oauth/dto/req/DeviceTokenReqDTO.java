package site.beilsang.beilsang_server_v2.domain.oauth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeviceTokenReqDTO {

    @Schema(description = " 디바이스 토큰", example = "fKx_..._xyz")
    @NotBlank(message = "디바이스 토큰은 필수입니다.")
    private String deviceToken;
}
