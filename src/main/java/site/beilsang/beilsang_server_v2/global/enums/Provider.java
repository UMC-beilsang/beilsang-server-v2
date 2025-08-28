package site.beilsang.beilsang_server_v2.global.enums;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Provider {
    KAKAO("kakao"), APPLE("naver");

    private final String provider;

    public static Provider getByName(String name) {
        //TODO
        return Arrays.stream(Provider.values())
            .filter(provider -> provider.getProvider().equals(name))
            .findAny().orElseThrow(() -> new RuntimeException());
    }
}
