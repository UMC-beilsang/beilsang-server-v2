package site.beilsang.beilsang_server_v2.domain.member.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@AllArgsConstructor
public class MemberProfileImageReqDTO {

    private MultipartFile profileImage;
}
