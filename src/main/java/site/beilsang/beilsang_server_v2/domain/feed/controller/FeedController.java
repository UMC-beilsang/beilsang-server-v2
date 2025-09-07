package site.beilsang.beilsang_server_v2.domain.feed.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedCreateReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedListReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedUpdateReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedCreateResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedDeleteResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedDetailResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedLikeResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedUpdateResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.service.FeedService;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;
import site.beilsang.beilsang_server_v2.global.common.PageResponseDTO;

@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
@Tag(name = "Feed", description = "피드 관리 API")
public class FeedController {

    private final FeedService feedService;

    @Operation(summary = "피드 목록 조회", description = "카테고리별로 필터링된 피드 목록을 페이지네이션으로 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "피드 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = PageResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public BaseResponse<PageResponseDTO<PreviewFeedResDTO>> getFeedList(
        Authentication authentication,
        @Parameter(description = "피드 목록 조회 필터 조건") @ModelAttribute FeedListReqDTO requestDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(feedService.getFeedList(memberId, requestDTO));
    }

    @Operation(summary = "피드 상세 조회", description = "특정 피드의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "피드 상세 조회 성공",
            content = @Content(schema = @Schema(implementation = FeedDetailResDTO.class))),
        @ApiResponse(responseCode = "404", description = "피드를 찾을 수 없음"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/{feedId}")
    public BaseResponse<FeedDetailResDTO> getFeedDetail(
        @Parameter(description = "피드 ID", example = "1") @PathVariable Long feedId,
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(feedService.getFeedDetail(feedId, memberId));
    }

    @Operation(summary = "피드 작성", description = "새로운 피드를 작성합니다. (챌린지 참여자만 가능)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "피드 작성 성공",
            content = @Content(schema = @Schema(implementation = FeedCreateResDTO.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "챌린지 참여자가 아님")
    })
    @PostMapping
    public BaseResponse<FeedCreateResDTO> createFeed(
        Authentication authentication,
        @Parameter(description = "피드 작성 요청 데이터") @RequestPart("data") FeedCreateReqDTO createReqDTO,
        @Parameter(description = "피드 이미지 파일") @RequestPart(value = "feedImage", required = false) MultipartFile feedImage) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(feedService.createFeed(memberId, createReqDTO, feedImage));
    }

    @Operation(summary = "피드 수정", description = "기존 피드를 수정합니다. (작성자만 가능)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "피드 수정 성공",
            content = @Content(schema = @Schema(implementation = FeedUpdateResDTO.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "피드 수정 권한 없음"),
        @ApiResponse(responseCode = "404", description = "피드를 찾을 수 없음")
    })
    @PutMapping("/{feedId}")
    public BaseResponse<FeedUpdateResDTO> updateFeed(
        @Parameter(description = "수정할 피드 ID", example = "1") @PathVariable Long feedId,
        Authentication authentication,
        @Parameter(description = "피드 수정 요청 데이터") @RequestPart("data") FeedUpdateReqDTO updateReqDTO,
        @Parameter(description = "새로운 피드 이미지 파일") @RequestPart(value = "feedImage", required = false) MultipartFile feedImage) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(
            feedService.updateFeed(feedId, memberId, updateReqDTO, feedImage));
    }

    @Operation(summary = "피드 삭제", description = "피드를 삭제합니다. (작성자만 가능)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "피드 삭제 성공",
            content = @Content(schema = @Schema(implementation = FeedDeleteResDTO.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "피드 삭제 권한 없음"),
        @ApiResponse(responseCode = "404", description = "피드를 찾을 수 없음")
    })
    @DeleteMapping("/{feedId}")
    public BaseResponse<FeedDeleteResDTO> deleteFeed(
        @Parameter(description = "삭제할 피드 ID", example = "1") @PathVariable Long feedId,
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(feedService.deleteFeed(feedId, memberId));
    }

    @Operation(summary = "피드 좋아요 추가", description = "피드에 좋아요를 추가합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "피드 좋아요 추가 성공",
            content = @Content(schema = @Schema(implementation = FeedLikeResDTO.class))),
        @ApiResponse(responseCode = "400", description = "이미 좋아요를 누른 피드"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "피드를 찾을 수 없음")
    })
    @PostMapping("/{feedId}/like")
    public BaseResponse<FeedLikeResDTO> addFeedLike(
        @Parameter(description = "좋아요를 추가할 피드 ID", example = "1") @PathVariable Long feedId,
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(feedService.addFeedLike(feedId, memberId));
    }

    @Operation(summary = "피드 좋아요 취소", description = "피드의 좋아요를 취소합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "피드 좋아요 취소 성공",
            content = @Content(schema = @Schema(implementation = FeedLikeResDTO.class))),
        @ApiResponse(responseCode = "400", description = "좋아요를 누르지 않은 피드"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "피드를 찾을 수 없음")
    })
    @DeleteMapping("/{feedId}/like")
    public BaseResponse<FeedLikeResDTO> removeFeedLike(
        @Parameter(description = "좋아요를 취소할 피드 ID", example = "1") @PathVariable Long feedId,
        Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(feedService.removeFeedLike(feedId, memberId));
    }

    @Operation(summary = "내 피드 목록 조회", description = "현재 사용자가 작성한 피드 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "내 피드 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = PageResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/my")
    public BaseResponse<PageResponseDTO<PreviewFeedResDTO>> getMyFeedList(
        Authentication authentication,
        @Parameter(description = "페이지 번호", example = "0") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(feedService.getMyFeedList(memberId, page, size));
    }
}
