package site.beilsang.beilsang_server_v2.global.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class AppleRevokeReq {
    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("token")
    private String token;

    @JsonProperty("token_type_hint")
    private String tokenTypeHint;
}
