package site.beilsang.beilsang_server_v2.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;

@RestController
@RequestMapping("/api/login")
@Tag(name = "Member Login", description = "회원 로그인 및 회원가입 관련 API")
public class MemberLoginController {
    /**
     * 토큰 업데이트, 회원가입과 관련된 컨트롤러
     */
    @Operation(summary = "닉네임 유효성 검사", description = "회원가입 시 닉네임 유효성을 검사합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "닉네임 유효성 검사 완료"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 닉네임")
    })
    @GetMapping("/nickname")
    public BaseResponse<Void> validateNickname(){
        return new BaseResponse<>();
    }
}
