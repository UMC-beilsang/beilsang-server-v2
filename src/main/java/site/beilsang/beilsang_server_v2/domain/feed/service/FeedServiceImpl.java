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
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.global.aws.s3.S3Service;
import site.beilsang.beilsang_server_v2.global.common.PageResponseDTO;
import site.beilsang.beilsang_server_v2.global.enums.UploadPath;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final S3Service s3Service;

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

        // FeedAssembler를 사용하여 FeedDetailResDTO 생성
        return FeedAssembler.toFeedDetailResDTO(feed, isLiked);
    }

    @Override
    @Transactional
    public FeedCreateResDTO createFeed(Long memberId, FeedCreateReqDTO createReqDTO,
        MultipartFile feedImage) {
        // ChallengeMember 조회 및 권한 검증
        ChallengeMember challengeMember = challengeMemberRepository.findById(
                createReqDTO.getChallengeMemberId())
            .orElseThrow(() -> new IllegalArgumentException(
                "챌린지 멤버를 찾을 수 없습니다. ID: " + createReqDTO.getChallengeMemberId()));

        // 작성자가 해당 챌린지의 참여자인지 확인
        if (!challengeMember.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("해당 챌린지의 참여자만 피드를 작성할 수 있습니다.");
        }

        // 파일이 없거나 빈 파일인 경우 예외 발생
        if (feedImage == null || feedImage.isEmpty()) {
            throw new IllegalArgumentException("이미지가 비어 있습니다.");
        }
        String feedUrl = s3Service.uploadFile(UploadPath.FEED, feedImage);

        // Feed 엔티티 생성
        Feed feed = Feed.builder()
            .review(createReqDTO.getReview())
            .uploadDate(createReqDTO.getUploadDate())
            .feedUrl(feedUrl)
            .challenge(challengeMember.getChallenge())
            .challengeMember(challengeMember)
            .build();

        // Feed 저장
        Feed savedFeed = feedRepository.save(feed);

        // FeedAssembler를 사용하여 FeedCreateResDTO 생성 및 반환
        return FeedAssembler.toFeedCreateResDTO(savedFeed);
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
