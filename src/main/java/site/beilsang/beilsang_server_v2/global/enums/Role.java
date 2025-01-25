package site.beilsang.beilsang_server_v2.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {

    GUEST("GUEST"),
    USER("USER"),
    ADMIN("USER");

    private final String role;
}