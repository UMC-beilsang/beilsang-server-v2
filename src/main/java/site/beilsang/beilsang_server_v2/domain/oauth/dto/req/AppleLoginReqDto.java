package site.beilsang.beilsang_server_v2.domain.oauth.dto.req;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AppleLoginReqDto {
    private String identityToken;
}
