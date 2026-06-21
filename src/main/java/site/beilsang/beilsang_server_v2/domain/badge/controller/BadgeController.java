package site.beilsang.beilsang_server_v2.domain.badge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import site.beilsang.beilsang_server_v2.domain.badge.dto.res.BadgeResDTO;
import site.beilsang.beilsang_server_v2.domain.badge.dto.res.RepresentativeBadgeResDTO;
import site.beilsang.beilsang_server_v2.domain.badge.service.BadgeService;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/badge")
@Tag(name = "Badge", description = "배지 API")
public class BadgeController {

    private final BadgeService badgeService;

    // ── 내 배지 전체 목록 조회 ────────────────────────────────────────────────
    @Operation(
        summary = "배지 전체 목록 조회",
        description = "챌린저가 보유한 전체 배지 목록을 반환합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "배지 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
    })
    @GetMapping("/{challengerId}")
    public BaseResponse<List<BadgeResDTO>> getMyBadgeList(Authentication authentication, @PathVariable Long challengerId) {
        return new BaseResponse<>(badgeService.getMyBadgeList(challengerId));
    }

    // ── 대표 배지 조회 ────────────────────────────────────────────────────────
    @Operation(
        summary = "대표 배지 조회",
        description = "사용자의 대표 배지를 반환합니다. 미설정 시 null을 반환합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "대표 배지 조회 성공 (미설정 시 null)"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
    })
    @GetMapping("/representative")
    public BaseResponse<BadgeResDTO> getRepresentativeBadge(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(badgeService.getRepresentativeBadge(memberId));
    }

    // ── 대표 배지 설정 / 해제 ─────────────────────────────────────────────────
    @Operation(
        summary = "대표 배지 설정 / 해제",
        description = "4단계(숲) 카테고리 배지를 대표 배지로 설정합니다. memberBadgeId를 null로 전달하면 대표 배지가 해제됩니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "대표 배지 설정/해제 성공"),
        @ApiResponse(responseCode = "400", description = "4단계 미달 배지로 설정 시도"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "멤버 또는 배지를 찾을 수 없음")
    })
    @PatchMapping("/representative")
    public BaseResponse<Void> updateRepresentativeBadge(
        Authentication authentication,
        @RequestBody RepresentativeBadgeResDTO requestDTO
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        badgeService.updateRepresentativeBadge(memberId, requestDTO);
        return new BaseResponse<>();
    }
}
