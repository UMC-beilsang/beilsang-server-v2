package site.beilsang.beilsang_server_v2.global.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Role {
    USER("USER"),
    ADMIN("USER");

    private final String role;
}