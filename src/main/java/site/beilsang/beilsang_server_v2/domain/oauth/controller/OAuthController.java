package site.beilsang.beilsang_server_v2.domain.oauth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberLoginResDTO;
import site.beilsang.beilsang_server_v2.domain.oauth.dto.req.AppleLoginReqDTO;
import site.beilsang.beilsang_server_v2.domain.oauth.dto.req.DeviceTokenReqDTO;
import site.beilsang.beilsang_server_v2.domain.oauth.dto.req.KakaoLoginReqDTO;
import site.beilsang.beilsang_server_v2.domain.oauth.dto.req.RefreshTokenReqDTO;
import site.beilsang.beilsang_server_v2.domain.oauth.service.OAuthService;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Tag(name = "OAuth", description = "OAuth 소셜 로그인 API")
public class OAuthController {

    private final OAuthService oAuthService;

    @Operation(
        summary = "카카오 소셜 로그인",
        description = """
            iOS SDK에서 로그인 후 전달받은 Kakao 토큰(idToken 또는 accessToken)을 이용하여
            서버에서 카카오 인증을 검증하고, 신규 회원은 가입 처리 후 JWT 토큰을 발급합니다.
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "카카오 로그인 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = MemberLoginResDTO.class)
        )
    )
    @PostMapping("/login/kakao")
    public BaseResponse<MemberLoginResDTO> loginWithKakao(@RequestBody KakaoLoginReqDTO request) {
        return new BaseResponse<>(oAuthService.loginWithKakao(request));
    }

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
                      "message": "요청에 성공하였습니다."
                    }
                    """
            )
        )
    )
    @PostMapping("/login/apple")
    public BaseResponse<MemberLoginResDTO> loginWithApple(@RequestBody AppleLoginReqDTO request) {
        return new BaseResponse<>(oAuthService.loginWithApple(request));
    }

    @Operation(
        summary = "카카오 로그아웃",
        description = """
            사용자를 카카오 계정에서 로그아웃 처리합니다.

            - 서버에 저장된 Refresh Token을 제거하여 재로그인을 요구합니다.
            - Kakao API(`/v1/user/logout`)를 호출하여 카카오 서버에서도 로그아웃이 처리됩니다.
            - Access Token은 클라이언트 단에서 제거해야 합니다.
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "카카오 로그아웃 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BaseResponse.class),
            examples = @ExampleObject(
                name = "카카오 로그아웃 성공 예시",
                value = """
                    {
                      "isSuccess": true,
                      "code": "200",
                      "message": "요청에 성공하였습니다."
                    }
                    """
            )
        )
    )
    @PostMapping("/logout/kakao")
    public BaseResponse<Void> logoutWithKakao(
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        oAuthService.logoutWithKakao(memberId);
        return new BaseResponse<>();
    }

    @Operation(
        summary = "카카오 연동 해제(탈퇴)",
        description = """
            사용자의 카카오 계정을 서비스와 완전히 연동 해제합니다.

            - Kakao API(`/v1/user/unlink`) 호출로 카카오 측에서 서비스 연결이 제거됩니다.
            - 서버에서는 회원의 provider 정보 및 socialId 기반으로 처리합니다.
            - 이 작업은 돌이킬 수 없으며, 사용자가 다시 카카오 로그인이 필요합니다.
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "카카오 탈퇴(연동 해제) 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BaseResponse.class),
            examples = @ExampleObject(
                name = "카카오 탈퇴 성공 예시",
                value = """
                    {
                      "isSuccess": true,
                      "code": "200",
                      "message": "요청에 성공하였습니다."
                    }
                    """
            )
        )
    )
    @PostMapping("/unlink/kakao")
    public BaseResponse<Void> unlinkWithKakao(
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        oAuthService.unlinkWithKakao(memberId);
        return new BaseResponse<>();
    }

    @Operation(
        summary = "Apple 연동 해제(탈퇴)",
        description = """
            사용자의 Apple 계정과 서비스 간의 연결을 해제하고,
            서버의 회원 정보를 완전히 삭제합니다.
            Apple 특성상 refreshToken 기반 또는 identityToken 기반 두 방식이 존재합니다.
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "애플 탈퇴(연동 해제) 성공"
    )
    @PostMapping("/unlink/apple")
    public BaseResponse<Void> unlinkWithApple(
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        oAuthService.unlinkWithApple(memberId);
        return new BaseResponse<>();
    }

    @Operation(
        summary = "JWT 토큰 재발급",
        description = """
            만료된 Access Token 대신 새로운 Access Token과 Refresh Token을 발급합니다.
            """
    )
    @ApiResponse(
        responseCode = "200",
        description = "토큰 재발급 성공",
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
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR...",
                        "Role": "Guest"
                      }
                    }
                    """
            )
        )
    )
    @PostMapping("/refresh")
    public BaseResponse<MemberLoginResDTO> refreshToken(
        @RequestBody RefreshTokenReqDTO refreshTokenReqDTO) {
        return new BaseResponse<>(oAuthService.refreshToken(refreshTokenReqDTO.refreshToken()));
    }

    /**
     * 토큰 업데이트, 회원가입과 관련된 컨트롤러
     */
    @Operation(summary = "닉네임 유효성 검사", description = "회원가입 시 닉네임 유효성을 검사합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "닉네임 유효성 검사 완료"),
        @ApiResponse(responseCode = "400", description = "유효하지 않은 닉네임")
    })
    @GetMapping("/nickname")
    public BaseResponse<Void> validateNickname(@RequestParam String nickname) {
        oAuthService.validateNickname(nickname);
        return new BaseResponse<>();
    }

    @Operation(summary = "디바이스 토큰 갱신", description = "푸시 알림 수신을 위한 FCM 디바이스 토큰을 갱신합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/device-token")
    public BaseResponse<Void> updateDeviceToken(
        Authentication authentication,
        @Valid @RequestBody DeviceTokenReqDTO reqDto
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        oAuthService.updateDeviceToken(memberId, reqDto.getDeviceToken());

        return new BaseResponse<>();
    }
}
