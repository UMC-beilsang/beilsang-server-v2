package site.beilsang.beilsang_server_v2.domain.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;

@RestController
@RequestMapping("/api/login")
public class MemberLoginController {
    /**
     * 토큰 업데이트, 회원가입과 관련된 컨트롤러
     */
    @GetMapping("/nickname")
    public BaseResponse<Void> validateNickname(){
        return new BaseResponse<>();
    }
}
