package site.beilsang.beilsang_server_v2.domain.oauth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeviceTokenReqDTO {

    @Schema(description = " 디바이스 토큰", example = "fKx_..._xyz")
    private String deviceToken;
}
