package site.beilsang.beilsang_server_v2.domain.oauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberLoginResDTO;
import site.beilsang.beilsang_server_v2.domain.oauth.dto.req.AppleLoginReqDto;
import site.beilsang.beilsang_server_v2.domain.oauth.service.OAuthService;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Tag(name = "OAuth", description = "OAuth 소셜 로그인 API")
public class OAuthController {

    private final OAuthService oAuthService;

    /**
     * 애플 로그인 - identityToken을 받아 로그인 처리
     *
     * @param request identityToken을 포함한 요청
     * @return JWT 토큰
     */
    @Operation(
        summary = "Apple 소셜 로그인",
        description = "Apple ID로 로그인하여 JWT 토큰을 발급받습니다. iOS 앱에서 받은 identityToken을 전송해주세요."
    )
    @ApiResponse(
        responseCode = "200",
        description = "로그인 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = MemberLoginResDTO.class),
            examples = @ExampleObject(
                name = "성공 응답 예시",
                value = """
                    {
                      "isSuccess": true,
                      "code": "200",
                      "message": "요청에 성공하였습니다.",
                      "result": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "Role": Guest
                      }
                    }
                    """
            )
        )
    )
    @PostMapping("/login/apple")
    public BaseResponse<MemberLoginResDTO> LoginWithApple(@RequestBody AppleLoginReqDto request) {
        return new BaseResponse<>(oAuthService.loginWithApple(request));
    }

    @PostMapping("/logout/kakao")
    public BaseResponse<Void> logoutWithKakao(
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        oAuthService.logoutWithKakao(memberId);
        return new BaseResponse<>();
    }

    @PostMapping("/unlink/kakao")
    public BaseResponse<Void> unlink(
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        oAuthService.unlinkWithKakao(memberId);
        return new BaseResponse<>();
    }
}
