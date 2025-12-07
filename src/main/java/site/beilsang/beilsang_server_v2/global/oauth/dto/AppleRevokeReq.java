package site.beilsang.beilsang_server_v2.global.oauth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class AppleRevokeReq {
    private String client_id;
    private String client_secret;
    private String token;
    private String token_type_hint;
}
