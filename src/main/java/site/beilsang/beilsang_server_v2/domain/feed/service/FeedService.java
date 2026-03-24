package site.beilsang.beilsang_server_v2.domain.feed.service;

import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedCreateReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedListReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedSearchReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedCreateResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedDetailResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedLikeResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedResDTO;
import site.beilsang.beilsang_server_v2.global.common.SliceResponseDTO;
import site.beilsang.beilsang_server_v2.global.enums.Category;

/**
 * 피드 서비스 인터페이스 피드 관련 비즈니스 로직을 정의합니다.
 */
public interface FeedService {

    /**
     * 피드 목록을 조회합니다. 카테고리별 필터링 및 무한 스크롤을 지원합니다.
     *
     * @param memberId   요청한 사용자 ID (좋아요 상태 확인용)
     * @param requestDTO 피드 목록 조회 요청 DTO (카테고리, 페이징 정보)
     * @return 슬라이스된 피드 목록
     */
    SliceResponseDTO<PreviewFeedResDTO> getFeedList(Long memberId, FeedListReqDTO requestDTO);

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
     * 내가 작성한 피드 목록을 조회합니다. 카테고리별 필터링을 지원합니다.
     *
     * @param memberId 조회할 사용자 ID
     * @param category 필터링할 카테고리 (null 또는 ALL인 경우 전체 조회)
     * @param page     페이지 번호
     * @param size     페이지 크기
     * @return 슬라이스된 내 피드 목록 (무한 스크롤용)
     */
    SliceResponseDTO<PreviewFeedResDTO> getMyFeedList(Long memberId, Category category, int page,
        int size);

    /**
     * 피드를 검색합니다. 피드 내용(review)을 키워드로 검색하고, 등록 시간 기준으로 정렬합니다.
     *
     * @param memberId   요청한 사용자 ID (좋아요 상태 확인용)
     * @param requestDTO 피드 검색 요청 DTO (keyword, sortType, page, size)
     * @return 슬라이스된 피드 검색 결과 (무한 스크롤용)
     */
    SliceResponseDTO<PreviewFeedResDTO> searchFeeds(Long memberId, FeedSearchReqDTO requestDTO);

    /**
     * 특정 챌린지의 전체 피드 목록을 조회합니다.
     *
     * @param challengeId 조회할 챌린지 ID
     * @param memberId    요청한 사용자 ID (isMyFeed 계산용)
     * @param page        페이지 번호
     * @param size        페이지 크기
     * @return 슬라이스된 피드 목록 (무한 스크롤용)
     */
    SliceResponseDTO<PreviewFeedResDTO> getChallengeFeedList(Long challengeId, Long memberId,
        int page, int size);

    /**
     * 내가 참여한 챌린지에서 내가 작성한 피드 목록을 조회합니다. 챌린지 참여자만 조회할 수 있습니다.
     *
     * @param challengeId 조회할 챌린지 ID
     * @param memberId    요청한 사용자 ID
     * @param page        페이지 번호
     * @param size        페이지 크기
     * @return 슬라이스된 내 피드 목록 (무한 스크롤용)
     */
    SliceResponseDTO<PreviewFeedResDTO> getMyChallengeFeedList(Long challengeId, Long memberId,
        int page, int size);
}
