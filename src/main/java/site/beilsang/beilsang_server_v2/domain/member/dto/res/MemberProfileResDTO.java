package site.beilsang.beilsang_server_v2.domain.member.dto.res;

import lombok.Builder;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.global.enums.Gender;

import java.time.LocalDate;

@Getter
@Builder
public class MemberProfileResDTO {
    private String nickName;
    private LocalDate birth;
    private Gender gender;
    private String address;
}
