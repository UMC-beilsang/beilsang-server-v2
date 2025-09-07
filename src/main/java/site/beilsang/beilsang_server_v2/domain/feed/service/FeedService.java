package site.beilsang.beilsang_server_v2.domain.feed.service;

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
import site.beilsang.beilsang_server_v2.global.common.PageResponseDTO;

/**
 * 피드 서비스 인터페이스 피드 관련 비즈니스 로직을 정의합니다.
 */
public interface FeedService {

    /**
     * 피드 목록을 조회합니다. 카테고리별 필터링 및 페이지네이션을 지원합니다.
     *
     * @param memberId   요청한 사용자 ID (좋아요 상태 확인용)
     * @param requestDTO 피드 목록 조회 요청 DTO (카테고리, 페이징 정보)
     * @return 페이지네이션된 피드 목록
     */
    PageResponseDTO<PreviewFeedResDTO> getFeedList(Long memberId, FeedListReqDTO requestDTO);

    /**
     * 피드 상세 정보를 조회합니다.
     *
     * @param feedId   조회할 피드 ID
     * @param memberId 요청한 사용자 ID (좋아요 상태 확인용)
     * @return 피드 상세 정보
     */
    FeedDetailResDTO getFeedDetail(Long feedId, Long memberId);

    /**
     * 새로운 피드를 작성합니다. 챌린지 참여자만 작성 가능합니다.
     *
     * @param memberId     작성자 ID
     * @param createReqDTO 피드 작성 요청 DTO
     * @param feedImage    피드 이미지 파일 (선택사항)
     * @return 생성된 피드 정보
     */
    FeedCreateResDTO createFeed(Long memberId, FeedCreateReqDTO createReqDTO,
        MultipartFile feedImage);

    /**
     * 기존 피드를 수정합니다. 작성자만 수정 가능합니다.
     *
     * @param feedId       수정할 피드 ID
     * @param memberId     요청한 사용자 ID
     * @param updateReqDTO 피드 수정 요청 DTO
     * @param feedImage    새로운 피드 이미지 파일 (선택사항)
     * @return 수정된 피드 정보
     */
    FeedUpdateResDTO updateFeed(Long feedId, Long memberId, FeedUpdateReqDTO updateReqDTO,
        MultipartFile feedImage);

    /**
     * 피드를 삭제합니다. 작성자만 삭제 가능합니다.
     *
     * @param feedId   삭제할 피드 ID
     * @param memberId 요청한 사용자 ID
     * @return 삭제된 피드 정보
     */
    FeedDeleteResDTO deleteFeed(Long feedId, Long memberId);

    /**
     * 피드에 좋아요를 추가합니다. 이미 좋아요를 누른 경우 예외가 발생합니다.
     *
     * @param feedId   좋아요를 추가할 피드 ID
     * @param memberId 좋아요를 누르는 사용자 ID
     * @return 좋아요 추가 결과
     */
    FeedLikeResDTO addFeedLike(Long feedId, Long memberId);

    /**
     * 피드의 좋아요를 취소합니다. 좋아요를 누르지 않은 경우 예외가 발생합니다.
     *
     * @param feedId   좋아요를 취소할 피드 ID
     * @param memberId 좋아요를 취소하는 사용자 ID
     * @return 좋아요 취소 결과
     */
    FeedLikeResDTO removeFeedLike(Long feedId, Long memberId);

    /**
     * 내가 작성한 피드 목록을 조회합니다.
     *
     * @param memberId 조회할 사용자 ID
     * @param page     페이지 번호
     * @param size     페이지 크기
     * @return 페이지네이션된 내 피드 목록
     */
    PageResponseDTO<PreviewFeedResDTO> getMyFeedList(Long memberId, int page, int size);
}
