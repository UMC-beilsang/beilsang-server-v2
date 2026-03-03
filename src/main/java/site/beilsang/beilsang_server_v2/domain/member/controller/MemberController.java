package site.beilsang.beilsang_server_v2.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.beilsang.beilsang_server_v2.domain.member.dto.req.MemberProfileImageReqDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.req.MemberNicknameReqDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.req.TermsAgreementReqDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.CheckEnrolledResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberProfileResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MyPageResDTO;
import site.beilsang.beilsang_server_v2.domain.member.service.MemberService;
import site.beilsang.beilsang_server_v2.domain.point.dto.res.PointLogListResDTO;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Member", description = "회원 관리 및 마이페이지 API")
public class MemberController {

    /**
     * 마이페이지와 관련된 컨트롤러
     */
    private final MemberService memberService;

    @Operation(summary = "마이페이지 조회", description = "로그인한 사용자의 마이페이지 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "마이페이지 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/mypage")
    public BaseResponse<MyPageResDTO> getMyPage(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(memberService.getMyPage(memberId));
    }

    @Operation(summary = "포인트 내역 조회", description = "로그인한 사용자의 포인트 내역을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "포인트 내역 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/mypage/point")
    public BaseResponse<PointLogListResDTO> getPoingLog(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(memberService.getPointLog(memberId));
    }

    @Operation(summary = "닉네임 수정", description = "회원의 닉네임을 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "닉네임 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/nickname")
    public BaseResponse<MemberProfileResDTO> updateNickname(Authentication authentication,
        @Parameter(description = "닉네임 수정 요청 데이터") @Valid  @RequestBody MemberNicknameReqDTO memberNicknameReqDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(memberService.updateNickname(memberId, memberNicknameReqDTO));
    }

    @Operation(summary = "프로필 이미지 수정", description = "회원의 프로필 이미지를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "프로필 이미지 수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 이미지 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/profile/image")
    public BaseResponse<Void> updateProfileImage(Authentication authentication,
        @Parameter(description = "프로필 이미지 수정 요청 데이터") @RequestBody MemberProfileImageReqDTO memberProfileImageReqDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        memberService.updateProfileImage(memberId, memberProfileImageReqDTO);
        return new BaseResponse<>();
    }

    @Operation(summary = "챌린지 참여 여부 확인", description = "특정 챌린지에 회원이 참여했는지 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "참여 여부 확인 성공"),
        @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/check/{challengeId}")
    public BaseResponse<CheckEnrolledResDTO> checkIsMemberEnrolled(Authentication authentication,
        @Parameter(description = "참여 여부를 확인할 챌린지 ID", example = "1") @PathVariable(name = "challengeId") Long challengeId) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(memberService.checkEnroll(memberId, challengeId));
    }

    @Operation(summary = "약관 동의", description = "회원이 약관에 동의합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "약관 동의 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/terms/agree")
    public BaseResponse<Void> agreeToTerms(Authentication authentication,
        @Parameter(description = "약관 동의 요청 데이터") @Valid @RequestBody TermsAgreementReqDTO termsAgreementReqDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        memberService.agreeToTerms(memberId, termsAgreementReqDTO);
        return new BaseResponse<>();
    }
}
