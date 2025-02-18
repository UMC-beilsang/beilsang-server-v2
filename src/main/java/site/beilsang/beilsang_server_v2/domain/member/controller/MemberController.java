package site.beilsang.beilsang_server_v2.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.beilsang.beilsang_server_v2.domain.member.dto.req.MemberProfileReqDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberProfileResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MyPageResDTO;
import site.beilsang.beilsang_server_v2.domain.member.service.MemberService;
import site.beilsang.beilsang_server_v2.domain.point.dto.res.PointLogListResDTO;
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

    @GetMapping("/mypage/point")
    public BaseResponse<PointLogListResDTO> getPoingLog(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(memberService.getPointLog(memberId));
    }

    @PatchMapping("/profile")
    public BaseResponse<MemberProfileResDTO> updateProfile(Authentication authentication,
                                                           @RequestBody MemberProfileReqDTO memberProfileReqDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(memberService.updateProfile(memberId, memberProfileReqDTO));
    }
    @PatchMapping("/profile/image")
    public BaseResponse<MemberProfileResDTO> updateProfileImage(Authentication authentication,
                                                           @RequestBody MemberProfileReqDTO memberProfileReqDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(memberService.updateProfile(memberId, memberProfileReqDTO));
    }
}
