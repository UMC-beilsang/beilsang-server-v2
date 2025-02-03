package site.beilsang.beilsang_server_v2.domain.member.dto;

import lombok.Builder;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.global.enums.Role;

@Getter
@Builder
public class MemberLoginResDTO {
    private String accessToken;
    private String refreshToken;
    private Role role;
}
