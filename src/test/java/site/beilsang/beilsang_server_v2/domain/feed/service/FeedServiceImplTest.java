package site.beilsang.beilsang_server_v2.domain.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.badge.service.BadgeService;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeRepository;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedCreateReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.feed.repository.FeedLikeRepository;
import site.beilsang.beilsang_server_v2.domain.feed.repository.FeedRepository;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.global.aws.s3.S3Service;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.UploadPath;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedService 단위테스트")
class FeedServiceImplTest {

    @Mock
    private FeedRepository feedRepository;

    @Mock
    private FeedLikeRepository feedLikeRepository;

    @Mock
    private ChallengeMemberRepository challengeMemberRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private BadgeService badgeService;

    @InjectMocks
    private FeedServiceImpl feedService;

    private Challenge challenge;
    private Member member;
    private ChallengeMember challengeMember;
    private FeedCreateReqDTO requestDTO;
    private MultipartFile feedImage;

    @BeforeEach
    void setUp() {
        challenge = Challenge.builder()
            .id(1L)
            .title("테스트 챌린지")
            .category(Category.PLOGGING)
            .startDate(LocalDate.now().minusDays(1))
            .finishDate(LocalDate.now().plusDays(5))
            .status(ChallengeStatus.IN_PROGRESS)
            .isHidden(false)
            .build();

        member = Member.builder()
            .id(1L)
            .nickName("테스트유저")
            .build();

        challengeMember = ChallengeMember.builder()
            .id(10L)
            .member(member)
            .challenge(challenge)
            .isHost(false)
            .successDays(0)
            .challengeMemberStatus(ChallengeMemberStatus.ONGOING)
            .isFeedUpload(false)
            .build();

        requestDTO = new FeedCreateReqDTO();
        requestDTO.setChallengeMemberId(10L);
        requestDTO.setReview("오늘도 인증합니다.");

        feedImage = new MockMultipartFile("feedImage", "feed.jpg", "image/jpeg",
            "image-content".getBytes());
    }

    @Test
    @DisplayName("피드 생성 시 오늘 인증 플래그와 성공 일수가 1회만 증가한다")
    void createFeed_ShouldMarkDailyUploadAndIncreaseSuccessDaysOnce() {
        // given
        when(challengeMemberRepository.findById(10L)).thenReturn(Optional.of(challengeMember));
        when(s3Service.uploadFile(any(UploadPath.class), anyString(), any(MultipartFile.class)))
            .thenReturn("https://s3-url/feed.jpg");
        when(feedRepository.save(any(Feed.class))).thenAnswer(invocation -> {
            Feed feed = invocation.getArgument(0);
            return Feed.builder()
                .id(1L)
                .review(feed.getReview())
                .uploadDate(feed.getUploadDate())
                .feedUrl(feed.getFeedUrl())
                .challenge(feed.getChallenge())
                .challengeMember(feed.getChallengeMember())
                .build();
        });

        // when
        feedService.createFeed(member.getId(), requestDTO, feedImage);
        feedService.createFeed(member.getId(), requestDTO, feedImage);

        // then
        assertThat(challengeMember.getIsFeedUpload()).isTrue();
        assertThat(challengeMember.getSuccessDays()).isEqualTo(1);
        verify(feedRepository, times(2)).save(any(Feed.class));
        verify(badgeService, times(2)).grantActivityBadgeIfFirst(member.getId(),
            site.beilsang.beilsang_server_v2.global.enums.BadgeType.CHALLENGE_VERIFY);
    }
}


