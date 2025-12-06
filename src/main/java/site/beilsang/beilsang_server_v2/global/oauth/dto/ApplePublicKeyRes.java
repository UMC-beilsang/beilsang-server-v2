package site.beilsang.beilsang_server_v2.global.oauth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
public class ApplePublicKeyRes {
    private List<ApplePublicKey> keys;

    @Getter
    @NoArgsConstructor
    public static class ApplePublicKey {
        private String kty;
        private String kid;
        private String use;
        private String alg;
        private String n;
        private String e;
    }
}
