package site.beilsang.beilsang_server_v2.global.oauth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppleTokenRes(

    @JsonProperty(value = "access_token")
    @Schema(description = "애플 access_token") String accessToken,

    @JsonProperty(value = "expires_in")
    @Schema(description = "애플 토큰 만료 기한 expires_in") String expiresIn,

    @JsonProperty(value = "id_token")
    @Schema(description = "애플 id_token") String idToken,

    @JsonProperty(value = "refresh_token")
    @Schema(description = "애플 refresh_token") String refreshToken,

    @JsonProperty(value = "token_type")
    @Schema(description = "애플 token_type") String tokenType,

    @Schema(description = "error") String error
) {
}
