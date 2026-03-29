package site.beilsang.beilsang_server_v2.domain.feed.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeRepository;
import site.beilsang.beilsang_server_v2.domain.feed.dto.FeedAssembler;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedCreateReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedListReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedSearchReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedCreateResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedDetailResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedLikeResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.feed.entity.FeedLike;
import site.beilsang.beilsang_server_v2.domain.feed.repository.FeedLikeRepository;
import site.beilsang.beilsang_server_v2.domain.feed.repository.FeedRepository;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.global.aws.s3.S3Service;
import site.beilsang.beilsang_server_v2.global.common.SliceResponseDTO;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.UploadPath;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedServiceImpl implements FeedService {

    private final FeedRepository feedRepository;
    private final FeedLikeRepository feedLikeRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final MemberRepository memberRepository;
    private final ChallengeRepository challengeRepository;
    private final S3Service s3Service;

    @Override
    public SliceResponseDTO<PreviewFeedResDTO> getFeedList(Long memberId,
        FeedListReqDTO requestDTO) {
        // 페이징 처리 (최신순 정렬)
        Pageable pageable = PageRequest.of(requestDTO.getPage(), requestDTO.getSize(),
            Sort.by("createdAt").descending());

        Slice<Feed> feedSlice;
        if (requestDTO.getCategory() == Category.ALL) {
            // 전체 조회
            feedSlice = feedRepository.findAll(pageable);
        } else {
            // 카테고리별 조회
            feedSlice = feedRepository.findAllByChallenge_Category(requestDTO.getCategory(),
                pageable);
        }

        // SliceResponseDTO로 변환
        return FeedAssembler.toSliceResponseDTO(feedSlice, memberId);
    }

    @Override
    public FeedDetailResDTO getFeedDetail(Long feedId, Long memberId) {
        // 피드 조회 (존재하지 않으면 예외 발생)
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다. ID: " + feedId));

        // 좋아요 여부 확인
        boolean isLiked = feedLikeRepository.existsByFeed_IdAndMember_Id(feedId, memberId);

        // FeedAssembler를 사용하여 FeedDetailResDTO 생성
        return FeedAssembler.toFeedDetailResDTO(feed, isLiked, memberId);
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

        // 챌린지 시작일 이전에는 피드 작성 불가
        if (LocalDate.now().isBefore(challengeMember.getChallenge().getStartDate())) {
            throw new BaseException(BaseResponseCode.CHALLENGE_NOT_STARTED);
        }

        // 챌린지 종료일 이후에는 피드 작성 불가
        if (LocalDate.now().isAfter(challengeMember.getChallenge().getFinishDate())) {
            throw new BaseException(BaseResponseCode.CHALLENGE_ENDED);
        }

        // 파일이 없거나 빈 파일인 경우 예외 발생
        if (feedImage == null || feedImage.isEmpty()) {
            throw new IllegalArgumentException("이미지가 비어 있습니다.");
        }
        String feedUrl = s3Service.uploadFile(UploadPath.FEED, feedImage);

        // Feed 엔티티 생성 (FeedAssembler 오버로딩 메서드 활용)
        Feed feed = FeedAssembler.toEntity(
            createReqDTO,
            feedUrl,
            challengeMember.getChallenge(),
            challengeMember
        );

        // Feed 저장
        Feed savedFeed = feedRepository.save(feed);

        // FeedAssembler를 사용하여 FeedCreateResDTO 생성 및 반환
        return FeedAssembler.toFeedCreateResDTO(savedFeed);
    }

    @Override
    @Transactional
    public FeedLikeResDTO addFeedLike(Long feedId, Long memberId) {
        // 피드 존재 여부 확인
        Feed feed = feedRepository.findById(feedId)
            .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다. ID: " + feedId));

        // 사용자 존재 여부 확인
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + memberId));

        // 이미 좋아요를 누른 상태인지 확인
        boolean alreadyLiked = feedLikeRepository.existsByFeed_IdAndMember_Id(feedId, memberId);
        if (alreadyLiked) {
            throw new IllegalArgumentException("이미 좋아요를 누른 피드입니다.");
        }

        // 좋아요 생성 및 저장
        FeedLike feedLike = FeedLike.builder()
            .feed(feed)
            .member(member)
            .build();
        feedLikeRepository.save(feedLike);

        // 현재 좋아요 수 계산
        long currentLikeCount = feedLikeRepository.countByFeed_Id(feedId);

        // 응답 DTO 생성
        return FeedLikeResDTO.builder()
            .feedId(feedId)
            .likeCount(currentLikeCount)
            .isLiked(true)
            .build();
    }

    @Override
    @Transactional
    public FeedLikeResDTO removeFeedLike(Long feedId, Long memberId) {
        // 피드 존재 여부 확인
        feedRepository.findById(feedId)
            .orElseThrow(() -> new IllegalArgumentException("피드를 찾을 수 없습니다. ID: " + feedId));

        // 좋아요 존재 여부 확인
        FeedLike feedLike = feedLikeRepository.findByFeed_IdAndMember_Id(feedId, memberId)
            .orElseThrow(() -> new IllegalArgumentException("좋아요를 누르지 않은 피드입니다."));

        // 좋아요 삭제
        feedLikeRepository.delete(feedLike);

        // 현재 좋아요 수 계산
        long currentLikeCount = feedLikeRepository.countByFeed_Id(feedId);

        // 응답 DTO 생성
        return FeedLikeResDTO.builder()
            .feedId(feedId)
            .likeCount(currentLikeCount)
            .isLiked(false)
            .build();
    }

    @Override
    public SliceResponseDTO<PreviewFeedResDTO> getMyFeedList(Long memberId, Category category,
        int page, int size) {
        // 사용자 존재 여부 확인
        memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + memberId));

        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size);

        Slice<Feed> feedSlice;

        // 카테고리 필터링 처리
        if (category == Category.ALL) {
            feedSlice = feedRepository.findAllByChallengeMember_Member_IdOrderByCreatedAtDesc(
                memberId, pageable);
        } else {
            // 특정 카테고리의 피드만 조회
            feedSlice = feedRepository.findAllByChallengeMember_Member_IdAndChallenge_CategoryOrderByCreatedAtDesc(
                memberId, category, pageable);
        }

        // SliceResponseDTO로 변환하여 반환
        return FeedAssembler.toSliceResponseDTO(feedSlice, memberId);
    }

    @Override
    public SliceResponseDTO<PreviewFeedResDTO> getMyChallengeFeedList(Long challengeId,
        Long memberId, int page, int size) {
        // 챌린지 존재 여부 확인
        if (!challengeRepository.existsById(challengeId)) {
            throw new BaseException(BaseResponseCode.NOT_FOUND_CHALLENGE);
        }

        // 챌린지 참여 여부 확인
        challengeMemberRepository.findByChallengeIdAndMemberId(challengeId, memberId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_JOINED_CHALLENGE));

        // 최신순 페이징 처리
        Pageable pageable = PageRequest.of(page, size);

        // 챌린지 ID + 멤버 ID 기준 피드 조회
        Slice<Feed> feedSlice =
            feedRepository.findAllByChallenge_IdAndChallengeMember_Member_IdOrderByCreatedAtDesc(
                challengeId, memberId, pageable);

        return FeedAssembler.toSliceResponseDTO(feedSlice, memberId);
    }

    @Override
    public SliceResponseDTO<PreviewFeedResDTO> getChallengeFeedList(Long challengeId, Long memberId,
        int page, int size) {
        // 챌린지 존재 여부 확인
        if (!challengeRepository.existsById(challengeId)) {
            throw new BaseException(BaseResponseCode.NOT_FOUND_CHALLENGE);
        }

        // 최신순 페이징 처리
        Pageable pageable = PageRequest.of(page, size);

        // 챌린지 ID 기준 피드 조회
        Slice<Feed> feedSlice = feedRepository.findAllByChallenge_IdOrderByCreatedAtDesc(
            challengeId, pageable);

        return FeedAssembler.toSliceResponseDTO(feedSlice, memberId);
    }

    @Override
    public SliceResponseDTO<PreviewFeedResDTO> searchFeeds(Long memberId,
        FeedSearchReqDTO requestDTO) {
        // 사용자 존재 여부 확인
        memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + memberId));

        // Pageable 객체 생성 (정렬은 Repository에서 sortType에 따라 처리)
        Pageable pageable = PageRequest.of(requestDTO.getPage(), requestDTO.getSize());

        // Repository의 searchFeeds 호출
        Slice<Feed> feedSlice = feedRepository.searchFeeds(
            requestDTO.getKeyword(),
            requestDTO.getSortType(),
            pageable
        );

        // SliceResponseDTO로 변환하여 반환
        return FeedAssembler.toSliceResponseDTO(feedSlice, memberId);
    }
}
