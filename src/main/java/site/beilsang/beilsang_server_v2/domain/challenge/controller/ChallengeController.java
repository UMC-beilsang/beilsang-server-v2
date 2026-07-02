package site.beilsang.beilsang_server_v2.domain.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ClosedChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.LikedChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.MyChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.OpenChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.RecommendedChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.SearchClosedChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.SearchOpenChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeDetailResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeListResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.JoinChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.service.ChallengeService;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;
import site.beilsang.beilsang_server_v2.global.common.PageResponseDTO;

@RestController
@RequestMapping("/challenge")
@RequiredArgsConstructor
@Tag(name = "Challenge", description = "챌린지 관리 API")
public class ChallengeController {

    private final ChallengeService challengeService;

    @Operation(summary = "챌린지 생성", description = "새로운 챌린지를 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "챌린지 생성 성공",
            content = @Content(schema = @Schema(implementation = ChallengeResDTO.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<ChallengeResDTO> createChallenge(
        Authentication authentication,
        @Parameter(description = "챌린지 생성 요청 데이터") @RequestPart("data") CreateChallengeReqDTO createChallengeReqDTO,
        @Parameter(description = "챌린지 정보 이미지들") @RequestPart(value = "infoImages") List<MultipartFile> infoImages,
        @Parameter(description = "인증 방법 이미지들") @RequestPart(value = "certImages") List<MultipartFile> certImages) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(
            challengeService.createChallenge(memberId, createChallengeReqDTO, infoImages,
                certImages));
    }

    @Operation(summary = "모집중 챌린지 목록 조회",
        description = "현재 모집중인 챌린지 목록을 조회합니다. 시작일이 오늘 이후인 챌린지를 대상으로 합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "모집중 챌린지 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/list/open")
    public BaseResponse<PageResponseDTO<ChallengeListResDTO>> getOpenChallengeList(
        Authentication authentication,
        @Parameter(description = "모집중 챌린지 목록 조회 조건") @Valid @ModelAttribute OpenChallengeListReqDTO requestDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(challengeService.getOpenChallengeList(memberId, requestDTO));
    }

    @Operation(summary = "모집마감 챌린지 목록 조회",
        description = "모집이 마감된 챌린지 목록을 조회합니다. 시작일이 오늘 이전인 챌린지를 대상으로 합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "모집마감 챌린지 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/list/closed")
    public BaseResponse<PageResponseDTO<ChallengeListResDTO>> getClosedChallengeList(
        Authentication authentication,
        @Parameter(description = "모집마감 챌린지 목록 조회 조건") @Valid @ModelAttribute ClosedChallengeListReqDTO requestDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(challengeService.getClosedChallengeList(memberId, requestDTO));
    }

    @Operation(summary = "찜한 챌린지 목록 조회",
        description = "내가 찜한 챌린지 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "찜한 챌린지 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/liked")
    public BaseResponse<PageResponseDTO<ChallengeListResDTO>> getLikedChallengeList(
        Authentication authentication,
        @Parameter(description = "찜한 챌린지 목록 조회 조건") @Valid @ModelAttribute LikedChallengeListReqDTO requestDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(challengeService.getLikedChallengeList(memberId, requestDTO));
    }

    @Operation(summary = "나의 챌린지 목록 조회",
        description = "내가 참여한 챌린지 목록을 상태별로 조회합니다. "
            + "ONGOING(참여중), SUCCESS(달성), FAIL(실패) 상태로 필터링합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "나의 챌린지 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/my")
    public BaseResponse<PageResponseDTO<ChallengeListResDTO>> getMyChallengeList(
        Authentication authentication,
        @Parameter(description = "나의 챌린지 목록 조회 조건") @Valid @ModelAttribute MyChallengeListReqDTO requestDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(challengeService.getMyChallengeList(memberId, requestDTO));
    }

    @Operation(summary = "추천 챌린지 조회",
        description = "현재 모집 중인 챌린지 중 좋아요가 많은 순으로 추천 챌린지를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "추천 챌린지 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/recommended")
    public BaseResponse<List<ChallengeListResDTO>> getRecommendedChallenges(
        Authentication authentication,
        @Parameter(description = "추천 챌린지 조회 조건") @Valid @ModelAttribute RecommendedChallengeReqDTO requestDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(challengeService.getRecommendedChallenges(memberId, requestDTO));
    }

    @Operation(summary = "챌린지 상세 조회", description = "특정 챌린지의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "챌린지 상세 조회 성공"),
        @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/{challengeId}")
    public BaseResponse<ChallengeDetailResDTO> getChallengeDetail(
        @Parameter(description = "챌린지 ID", example = "1") @PathVariable Long challengeId,
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(challengeService.getChallengeDetail(challengeId, memberId));
    }

    @Operation(summary = "챌린지 참여", description = "특정 챌린지에 참여합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "챌린지 참여 성공"),
        @ApiResponse(responseCode = "400", description = "이미 참여한 챌린지 또는 참여 불가능한 상태"),
        @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/{challengeId}/join")
    public BaseResponse<JoinChallengeResDTO> joinChallenge(
        @Parameter(description = "참여할 챌린지 ID", example = "1") @PathVariable Long challengeId,
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(challengeService.joinChallenge(challengeId, memberId));
    }

    @Operation(summary = "챌린지 찜하기", description = "특정 챌린지를 찜합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "챌린지 찜하기 성공"),
        @ApiResponse(responseCode = "400", description = "이미 찜한 챌린지"),
        @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/{challengeId}/like")
    public BaseResponse<Void> likeChallenge(
        @Parameter(description = "찜할 챌린지 ID", example = "1") @PathVariable Long challengeId,
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        challengeService.likeChallenge(challengeId, memberId);
        return new BaseResponse<>();
    }

    @Operation(summary = "챌린지 찜 취소", description = "특정 챌린지의 찜을 취소합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "챌린지 찜 취소 성공"),
        @ApiResponse(responseCode = "400", description = "찜하지 않은 챌린지"),
        @ApiResponse(responseCode = "404", description = "챌린지를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @DeleteMapping("/{challengeId}/like")
    public BaseResponse<Void> unlikeChallenge(
        @Parameter(description = "찜 취소할 챌린지 ID", example = "1") @PathVariable Long challengeId,
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        challengeService.unlikeChallenge(challengeId, memberId);
        return new BaseResponse<>();
    }

    @Operation(summary = "모집 마감 챌린지 검색",
        description = "모집이 마감된 챌린지를 제목으로 검색합니다. 오늘 날짜에 가까운 챌린지부터 표시됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/search/closed")
    public BaseResponse<PageResponseDTO<ChallengeListResDTO>> searchClosedChallenges(
        Authentication authentication,
        @Parameter(description = "모집 마감 챌린지 검색 조건") @Valid @ModelAttribute SearchClosedChallengeReqDTO requestDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(challengeService.searchClosedChallenges(memberId, requestDTO));
    }

    @Operation(summary = "모집 중인 챌린지 검색",
        description = "현재 모집 중인 챌린지를 제목으로 검색합니다. 마감 임박순 또는 최신순으로 정렬할 수 있습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "검색 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/search/open")
    public BaseResponse<PageResponseDTO<ChallengeListResDTO>> searchOpenChallenges(
        Authentication authentication,
        @Parameter(description = "모집 중인 챌린지 검색 조건") @Valid @ModelAttribute SearchOpenChallengeReqDTO requestDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(challengeService.searchOpenChallenges(memberId, requestDTO));
    }
}
