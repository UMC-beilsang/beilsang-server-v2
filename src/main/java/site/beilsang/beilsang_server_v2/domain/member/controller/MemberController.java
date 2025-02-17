package site.beilsang.beilsang_server_v2.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MyPageResDTO;
import site.beilsang.beilsang_server_v2.domain.member.service.MemberService;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {
    /**
     * 마이페이지와 관련된 컨트롤러
     */
    private final MemberService memberService;

    @GetMapping("/mypage")
    public BaseResponse<MyPageResDTO> getMyPage(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(memberService.getMyPage(memberId));
    }
}
