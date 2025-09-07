package site.beilsang.beilsang_server_v2.domain.feed.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.feed.dto.FeedAssembler;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedCreateReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedListReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedUpdateReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedCreateResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedDeleteResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedDetailResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedLikeResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedUpdateResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.feed.repository.FeedRepository;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.PreviewMemberInfoDTO;
import site.beilsang.beilsang_server_v2.global.common.PageResponseDTO;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;

    @Override
    public PageResponseDTO<PreviewFeedResDTO> getFeedList(Long memberId,
        FeedListReqDTO requestDTO) {
        // 페이징 처리 (최신순 정렬)
        Pageable pageable = PageRequest.of(requestDTO.getPage(), requestDTO.getSize(),
            Sort.by("createdAt").descending());

        Page<Feed> feedPage;
        if (requestDTO.getCategory() != null) {
            // 카테고리별 조회
            feedPage = feedRepository.findAllByChallenge_Category(requestDTO.getCategory(),
                pageable);
        } else {
            // 전체 조회
            feedPage = feedRepository.findAll(pageable);
        }

        // 피드 리스트를 DTO로 변환
        List<Feed> feedList = feedPage.getContent();
        List<PreviewFeedResDTO> feedDtoList = FeedAssembler.toEntities(feedList);

        // PageResponseDTO로 변환
        return PageResponseDTO.<PreviewFeedResDTO>builder()
            .content(feedDtoList)
            .page(feedPage.getNumber())
            .size(feedPage.getSize())
            .totalElements(feedPage.getTotalElements())
            .totalPages(feedPage.getTotalPages())
            .hasNext(feedPage.hasNext())
            .build();
    }

    // TODO: 나머지 메소드들은 순차적으로 구현 예정
    @Override
    public FeedDetailResDTO getFeedDetail(Long feedId, Long memberId) {
        // 피드 조회 (존재하지 않으면 예외 발생)
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다. ID: " + feedId));

        // 좋아요 여부 확인 (TODO: FeedLike 레포지토리 구현 후 실제 로직 추가)
        boolean isLiked = false; // 임시로 false 처리

        // 좋아요 수 계산
        long likeCount = feed.getFeedLikes().size();

        // 챌린지 진행 일수 계산 (업로드 날짜 기준)
        long challengeDay = java.time.temporal.ChronoUnit.DAYS.between(
            feed.getChallenge().getStartDate(),
            feed.getUploadDate()
        ) + 1; // 1일차부터 시작

        // FeedDetailResDTO 생성
        return FeedDetailResDTO.builder()
            .feedId(feed.getId())
            .memberInfo(PreviewMemberInfoDTO.builder()
                .memberId(feed.getChallengeMember().getMember().getId())
                .nickName(feed.getChallengeMember().getMember().getNickName())
                .profileImage(feed.getChallengeMember().getMember().getProfileUrl())
                .build())
            .challengeId(feed.getChallenge().getId())
            .challengeTitle(feed.getChallenge().getTitle())
            .challengeCategory(feed.getChallenge().getCategory().name())
            .review(feed.getReview())
            .feedUrl(feed.getFeedUrl())
            .uploadDate(feed.getUploadDate())
            .likeCount(likeCount)
            .isLiked(isLiked)
            .createdAt(feed.getCreatedAt())
            .updatedAt(feed.getUpdatedAt())
            .build();
    }

    @Override
    public FeedCreateResDTO createFeed(Long memberId, FeedCreateReqDTO createReqDTO,
        MultipartFile feedImage) {
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }

    @Override
    public FeedUpdateResDTO updateFeed(Long feedId, Long memberId, FeedUpdateReqDTO updateReqDTO,
        MultipartFile feedImage) {
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }

    @Override
    public FeedDeleteResDTO deleteFeed(Long feedId, Long memberId) {
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }

    @Override
    public FeedLikeResDTO addFeedLike(Long feedId, Long memberId) {
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }

    @Override
    public FeedLikeResDTO removeFeedLike(Long feedId, Long memberId) {
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }

    @Override
    public PageResponseDTO<PreviewFeedResDTO> getMyFeedList(Long memberId, int page, int size) {
        throw new UnsupportedOperationException("아직 구현되지 않았습니다.");
    }
}
