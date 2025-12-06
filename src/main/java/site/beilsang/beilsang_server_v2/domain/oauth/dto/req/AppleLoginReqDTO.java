package site.beilsang.beilsang_server_v2.domain.oauth.dto.req;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AppleLoginReqDTO {
    private String identityToken;      // 사용자 정보 추출용
    private String authorizationCode;  // refresh token 발급용
}
