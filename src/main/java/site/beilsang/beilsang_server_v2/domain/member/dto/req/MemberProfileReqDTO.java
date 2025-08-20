package site.beilsang.beilsang_server_v2.domain.member.dto.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberProfileReqDTO {

    private String nickName;
    private String birth;
    private String gender;
    private String address;
    private String resolution;
}
