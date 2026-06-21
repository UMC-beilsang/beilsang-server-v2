package site.beilsang.beilsang_server_v2.domain.challenge.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeNoteRepository;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeRepository;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;
import site.beilsang.beilsang_server_v2.domain.point.repository.PointLogRepository;
import site.beilsang.beilsang_server_v2.global.aws.s3.S3Service;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;
import site.beilsang.beilsang_server_v2.global.enums.ChallengePeriod;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;
import site.beilsang.beilsang_server_v2.global.enums.UploadPath;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeService 단위테스트")
class ChallengeServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ChallengeRepository challengeRepository;

    @Mock
    private ChallengeMemberRepository challengeMemberRepository;

    @Mock
    private ChallengeNoteRepository challengeNoteRepository;

    @Mock
    private PointLogRepository pointLogRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private site.beilsang.beilsang_server_v2.domain.point.service.PointService pointService;

    @Mock
    private site.beilsang.beilsang_server_v2.domain.like.repository.ChallengeLikeRepository challengeLikeRepository;


    @InjectMocks
    private ChallengeServiceImpl challengeService;

    private Member testMember;
    private CreateChallengeReqDTO testCreateChallengeReqDTO;
    private Challenge challenge_NOT_YET;
    private Challenge challenge_ONGOING;
    private Challenge challenge_ENDED;
    private MultipartFile testMainImage;
    private MultipartFile testCertImage;
    private List<MultipartFile> testInfoImages;
    private List<MultipartFile> testCertImages;

    @BeforeEach
    void setUp() {
        // 테스트용 멤버 데이터 준비
        testMember = Member.builder()
            .id(1L)
            .nickName("테스트유저")
            .point(1000)
            .build();

        // 테스트용 챌린지 요청 DTO 준비
        testCreateChallengeReqDTO = createTestChallengeReqDTO();

        // 테스트용 챌린지 엔티티 준비 (필수 필드 모두 채움)
        challenge_NOT_YET = Challenge.builder()
            .id(1L)
            .title("테스트 챌린지")
            .category(Category.PLOGGING)
            .startDate(LocalDate.now().plusDays(1))
            .finishDate(LocalDate.now().plusDays(8))
            .joinPoint(100)
            .period(ChallengePeriod.WEEK)
            .totalGoalDay(5)
            .details("테스트 챌린지 설명")
            .status(ChallengeStatus.NOT_YET)
            .attendeeCount(1)
            .countLikes(0)
            .collectedPoint(100)
            .infoImages(new ArrayList<>())
            .certImages(new ArrayList<>())
            .challengeNotes(new ArrayList<>())
            .build();

        challenge_ONGOING = Challenge.builder()
            .id(1L)
            .title("테스트 챌린지")
            .category(Category.PLOGGING)
            .startDate(LocalDate.now().minusDays(1))
            .finishDate(LocalDate.now().plusDays(5))
            .joinPoint(100)
            .period(ChallengePeriod.WEEK)
            .totalGoalDay(5)
            .details("테스트 챌린지 설명")
            .status(ChallengeStatus.IN_PROGRESS)
            .attendeeCount(1)
            .countLikes(0)
            .collectedPoint(100)
            .infoImages(new ArrayList<>())
            .certImages(new ArrayList<>())
            .challengeNotes(new ArrayList<>())
            .build();

        challenge_ENDED = Challenge.builder()
            .id(1L)
            .title("테스트 챌린지")
            .category(Category.PLOGGING)
            .startDate(LocalDate.now().minusDays(5))
            .finishDate(LocalDate.now().minusDays(1))
            .joinPoint(100)
            .period(ChallengePeriod.WEEK)
            .totalGoalDay(5)
            .details("테스트 챌린지 설명")
            .status(ChallengeStatus.END)
            .attendeeCount(1)
            .countLikes(0)
            .collectedPoint(100)
            .infoImages(new ArrayList<>())
            .certImages(new ArrayList<>())
            .challengeNotes(new ArrayList<>())
            .build();

        // 테스트용 MultipartFile 준비
        testMainImage = new MockMultipartFile("mainImage", "main.jpg", "image/jpeg",
            "main image content".getBytes());
        testCertImage = new MockMultipartFile("certImage", "cert.jpg", "image/jpeg",
            "cert image content".getBytes());
        testInfoImages = Collections.singletonList(testMainImage);
        testCertImages = Collections.singletonList(testCertImage);
    }

    @Test
    @DisplayName("챌린지 생성 실패 - 존재하지 않는 멤버")
    void createChallenge_MemberNotFound() {
        // given
        Long nonExistentMemberId = 999L;
        when(memberRepository.findById(nonExistentMemberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> challengeService.createChallenge(
            nonExistentMemberId, testCreateChallengeReqDTO, testInfoImages, testCertImages))
            .isInstanceOf(BaseException.class)
            .hasFieldOrPropertyWithValue("baseResponseCode", BaseResponseCode.NOT_FOUND_MEMBER);

        // 멤버 조회만 수행되고 다른 작업은 수행되지 않음을 검증
        verify(memberRepository).findById(nonExistentMemberId);
        verifyNoInteractions(challengeRepository, challengeNoteRepository,
            challengeMemberRepository,
            pointLogRepository);
    }

    @Test
    @DisplayName("챌린지 생성 실패 - 포인트 부족")
    void createChallenge_NotEnoughPoint() {
        // given
        Long memberId = 1L;
        Member memberWithLowPoint = Member.builder()
            .id(memberId)
            .nickName("포인트부족유저")
            .point(50) // 요청 포인트(100)보다 적음
            .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(memberWithLowPoint));
        when(pointService.calculateValidPoints(memberId)).thenReturn(50); // 부족한 포인트

        // when & then
        assertThatThrownBy(() -> challengeService.createChallenge(
            memberId, testCreateChallengeReqDTO, testInfoImages, testCertImages))
            .isInstanceOf(BaseException.class)
            .hasFieldOrPropertyWithValue("baseResponseCode", BaseResponseCode.NOT_ENOUGH_POINT);

        // 멤버 조회만 수행되고 다른 작업은 수행되지 않음을 검증
        verify(memberRepository).findById(memberId);
        verifyNoInteractions(challengeRepository, challengeNoteRepository,
            challengeMemberRepository,
            pointLogRepository);
    }

    @Test
    @DisplayName("챌린지 생성 성공 - 챌린지 상태 NOT_YET (미래 시작일)")
    void createChallenge_StatusNotYet_WhenStartDateIsInFuture() {
        // given
        Long memberId = 1L;
        CreateChallengeReqDTO futureStartReqDTO = createTestChallengeReqDTOWithStartDate(
            LocalDate.now().plusDays(10));

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(pointService.calculateValidPoints(memberId)).thenReturn(200); // 충분한 포인트
        when(challengeRepository.save(any(Challenge.class))).thenReturn(challenge_NOT_YET);
        when(challengeNoteRepository.saveAll(any(List.class))).thenReturn(List.of());
        when(challengeMemberRepository.save(any(ChallengeMember.class))).thenReturn(
            mock(ChallengeMember.class));
        when(pointLogRepository.save(any(PointLog.class))).thenReturn(mock(PointLog.class));
        when(s3Service.uploadFile(any(UploadPath.class), any(String.class), any(MultipartFile.class)))
            .thenReturn("https://s3-url/test.jpg");

        // when
        ChallengeResDTO result = challengeService.createChallenge(memberId, futureStartReqDTO,
            testInfoImages,
            testCertImages);

        // then
        ArgumentCaptor<ChallengeMember> challengeMemberCaptor = ArgumentCaptor.forClass(
            ChallengeMember.class);
        verify(challengeMemberRepository).save(challengeMemberCaptor.capture());
        ChallengeMember savedChallengeMember = challengeMemberCaptor.getValue();
        assertThat(savedChallengeMember.getChallengeMemberStatus()).isEqualTo(
            ChallengeMemberStatus.NOT_YET);
        verify(s3Service, atLeastOnce()).uploadFile(any(UploadPath.class),
            any(String.class), any(MultipartFile.class));
    }

    @Test
    @DisplayName("챌린지 생성 성공 - 챌린지 상태 ONGOING (오늘 또는 과거 시작일)")
    void createChallenge_StatusOngoing_WhenStartDateIsToday() {
        // given
        Long memberId = 1L;
        CreateChallengeReqDTO todayStartReqDTO = createTestChallengeReqDTOWithStartDate(
            LocalDate.now());

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(pointService.calculateValidPoints(memberId)).thenReturn(200); // 충분한 포인트
        when(challengeRepository.save(any(Challenge.class))).thenReturn(challenge_ONGOING);
        when(challengeNoteRepository.saveAll(any(List.class))).thenReturn(List.of());
        when(challengeMemberRepository.save(any(ChallengeMember.class))).thenReturn(
            mock(ChallengeMember.class));
        when(pointLogRepository.save(any(PointLog.class))).thenReturn(mock(PointLog.class));
        when(s3Service.uploadFile(any(UploadPath.class), any(String.class), any(MultipartFile.class)))
            .thenReturn("https://s3-url/test.jpg");

        // when
        ChallengeResDTO result = challengeService.createChallenge(memberId, todayStartReqDTO,
            testInfoImages,
            testCertImages);

        // then
        ArgumentCaptor<ChallengeMember> challengeMemberCaptor = ArgumentCaptor.forClass(
            ChallengeMember.class);
        verify(challengeMemberRepository).save(challengeMemberCaptor.capture());
        ChallengeMember savedChallengeMember = challengeMemberCaptor.getValue();
        assertThat(savedChallengeMember.getChallengeMemberStatus()).isEqualTo(
            ChallengeMemberStatus.ONGOING);
        verify(s3Service, atLeastOnce()).uploadFile(any(UploadPath.class),
            any(String.class), any(MultipartFile.class));
    }

    @Test
    @DisplayName("챌린지 생성 성공 - 멤버 포인트 차감 확인")
    void createChallenge_MemberPointDeduction() {
        // given
        Long memberId = 1L;
        int initialPoint = 1000;
        int joinPoint = 100;
        Member memberWithPoint = Member.builder()
            .id(memberId)
            .nickName("테스트유저")
            .point(initialPoint)
            .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(memberWithPoint));
        when(pointService.calculateValidPoints(memberId)).thenReturn(200); // 충분한 포인트
        when(challengeRepository.save(any(Challenge.class))).thenReturn(challenge_NOT_YET);
        when(challengeNoteRepository.saveAll(any(List.class))).thenReturn(List.of());
        when(challengeMemberRepository.save(any(ChallengeMember.class))).thenReturn(
            mock(ChallengeMember.class));
        when(pointLogRepository.save(any(PointLog.class))).thenReturn(mock(PointLog.class));
        when(s3Service.uploadFile(any(UploadPath.class), any(String.class), any(MultipartFile.class)))
            .thenReturn("https://s3-url/test.jpg");

        // when
        ChallengeResDTO result = challengeService.createChallenge(memberId,
            testCreateChallengeReqDTO, testInfoImages,
            testCertImages);

        // then
        assertThat(memberWithPoint.getPoint()).isEqualTo(initialPoint - joinPoint);
        verify(s3Service, atLeastOnce()).uploadFile(any(UploadPath.class),
            any(String.class), any(MultipartFile.class));
    }

    @Test
    @DisplayName("참여하지 않은 사용자가 참여 가능한 챌린지 상세 조회")
    void getChallengeDetail_NotJoinedAndJoinable() {
        // given
        Long memberId = 2L; // 테스트 멤버(참여하지 않은 사용자)
        Long challengeId = 1L;

        when(challengeRepository.getChallengeByIdAndIsHiddenFalse(challengeId)).thenReturn(challenge_ONGOING);
        when(challengeMemberRepository.findByChallengeIdAndMemberId(challengeId,
            memberId)).thenReturn(Optional.empty());
        when(challengeMemberRepository.findByChallengeIdAndIsHost(challengeId, true))
            .thenReturn(Optional.empty());

        // when
        var result = challengeService.getChallengeDetail(challengeId, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getIsJoinable()).isTrue();
        assertThat(result.getStatus()).isEqualTo(ChallengeMemberStatus.NOT_JOINED);
        assertThat(result.getProgress()).isNull();
    }

    @Test
    @DisplayName("챌린지 참여 성공 - NOT_YET 상태 챌린지")
    void joinChallenge_Success_NotYetStatus() {
        // given
        Long challengeId = 1L;
        Long memberId = 2L;
        Integer remainingPoint = 900;

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge_NOT_YET));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(challengeMemberRepository.findByChallengeIdAndMemberId(challengeId,
            memberId)).thenReturn(Optional.empty());
        when(pointService.calculateValidPoints(memberId)).thenReturn(200, 100); // 충분한 포인트, 참여 후 포인트
        when(pointLogRepository.save(any(PointLog.class))).thenReturn(mock(PointLog.class));
        when(challengeMemberRepository.save(any(ChallengeMember.class))).thenReturn(
            mock(ChallengeMember.class));

        // when
        var result = challengeService.joinChallenge(challengeId, memberId);

        // then
        assertThat(result).isNotNull();

        // then
        verify(challengeRepository).findById(challengeId);
        verify(memberRepository).findById(memberId);
        verify(challengeMemberRepository).findByChallengeIdAndMemberId(challengeId, memberId);
        verify(pointService, times(2)).calculateValidPoints(memberId);
        verify(pointLogRepository).save(any(PointLog.class));
        verify(challengeMemberRepository).save(any(ChallengeMember.class));

        // ChallengeMember 저장 시 올바른 상태인지 확인
        ArgumentCaptor<ChallengeMember> challengeMemberCaptor = ArgumentCaptor.forClass(
            ChallengeMember.class);
        verify(challengeMemberRepository).save(challengeMemberCaptor.capture());
        ChallengeMember savedChallengeMember = challengeMemberCaptor.getValue();

        assertThat(savedChallengeMember.getIsHost()).isFalse();
        assertThat(savedChallengeMember.getSuccessDays()).isEqualTo(0);
        assertThat(savedChallengeMember.getChallengeMemberStatus()).isEqualTo(
            ChallengeMemberStatus.NOT_YET);
        assertThat(savedChallengeMember.getIsFeedUpload()).isFalse();
    }

    @Test
    @DisplayName("챌린지 참여 성공 - ONGOING 상태 챌린지")
    void joinChallenge_Success_OngoingStatus() {
        // given
        Long challengeId = 1L;
        Long memberId = 2L;
        Integer remainingPoint = 900;

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge_ONGOING));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(challengeMemberRepository.findByChallengeIdAndMemberId(challengeId,
            memberId)).thenReturn(Optional.empty());
        when(pointService.calculateValidPoints(memberId)).thenReturn(200, 100); // 충분한 포인트, 참여 후 포인트
        when(pointLogRepository.save(any(PointLog.class))).thenReturn(mock(PointLog.class));
        when(challengeMemberRepository.save(any(ChallengeMember.class))).thenReturn(
            mock(ChallengeMember.class));

        // when
        var result = challengeService.joinChallenge(challengeId, memberId);

        // then
        assertThat(result).isNotNull();

        // then
        // ChallengeMember 저장 시 올바른 상태인지 확인

        ArgumentCaptor<ChallengeMember> challengeMemberCaptor = ArgumentCaptor.forClass(
            ChallengeMember.class);
        verify(challengeMemberRepository).save(challengeMemberCaptor.capture());
        ChallengeMember savedChallengeMember = challengeMemberCaptor.getValue();

        assertThat(savedChallengeMember.getIsHost()).isFalse();
        assertThat(savedChallengeMember.getSuccessDays()).isEqualTo(0);
        assertThat(savedChallengeMember.getChallengeMemberStatus()).isEqualTo(
            ChallengeMemberStatus.ONGOING);
        assertThat(savedChallengeMember.getIsFeedUpload()).isFalse();
    }

    @Test
    @DisplayName("챌린지 참여 실패 - 존재하지 않는 챌린지")
    void joinChallenge_Fail_ChallengeNotFound() {
        // given
        Long challengeId = 999L;
        Long memberId = 2L;

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> challengeService.joinChallenge(challengeId, memberId))
            .isInstanceOf(BaseException.class)
            .hasFieldOrPropertyWithValue("baseResponseCode", BaseResponseCode.NOT_FOUND_CHALLENGE);

        verify(challengeRepository).findById(challengeId);
        verifyNoInteractions(memberRepository, challengeMemberRepository, pointService,
            pointLogRepository);
    }

    @Test
    @DisplayName("챌린지 참여 실패 - 존재하지 않는 멤버")
    void joinChallenge_Fail_MemberNotFound() {
        // given
        Long challengeId = 1L;
        Long memberId = 999L;

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge_NOT_YET));
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> challengeService.joinChallenge(challengeId, memberId))
            .isInstanceOf(BaseException.class)
            .hasFieldOrPropertyWithValue("baseResponseCode", BaseResponseCode.NOT_FOUND_MEMBER);

        verify(challengeRepository).findById(challengeId);
        verify(memberRepository).findById(memberId);
        verifyNoInteractions(challengeMemberRepository, pointService, pointLogRepository);
    }

    @Test
    @DisplayName("챌린지 참여 실패 - 이미 참여한 챌린지")
    void joinChallenge_Fail_AlreadyJoined() {
        // given
        Long challengeId = 1L;
        Long memberId = 2L;
        ChallengeMember existingMember = mock(ChallengeMember.class);

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge_NOT_YET));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(challengeMemberRepository.findByChallengeIdAndMemberId(challengeId, memberId))
            .thenReturn(Optional.of(existingMember));

        // when & then
        assertThatThrownBy(() -> challengeService.joinChallenge(challengeId, memberId))
            .isInstanceOf(BaseException.class)
            .hasFieldOrPropertyWithValue("baseResponseCode",
                BaseResponseCode.ALREADY_JOINED_CHALLENGE);

        verify(challengeRepository).findById(challengeId);
        verify(memberRepository).findById(memberId);
        verify(challengeMemberRepository).findByChallengeIdAndMemberId(challengeId, memberId);
        verifyNoInteractions(pointService, pointLogRepository);
    }

    @Test
    @DisplayName("챌린지 참여 실패 - 종료된 챌린지")
    void joinChallenge_Fail_ChallengeEnded() {
        // given
        Long challengeId = 1L;
        Long memberId = 2L;

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge_ENDED));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(challengeMemberRepository.findByChallengeIdAndMemberId(challengeId,
            memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> challengeService.joinChallenge(challengeId, memberId))
            .isInstanceOf(BaseException.class)
            .hasFieldOrPropertyWithValue("baseResponseCode", BaseResponseCode.CHALLENGE_ENDED);

        verify(challengeRepository).findById(challengeId);
        verify(memberRepository).findById(memberId);
        verify(challengeMemberRepository).findByChallengeIdAndMemberId(challengeId, memberId);
        verifyNoInteractions(pointService, pointLogRepository);
    }

    @Test
    @DisplayName("챌린지 참여 실패 - 포인트 부족")
    void joinChallenge_Fail_NotEnoughPoint() {
        // given
        Long challengeId = 1L;
        Long memberId = 2L;

        when(challengeRepository.findById(challengeId)).thenReturn(Optional.of(challenge_NOT_YET));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(challengeMemberRepository.findByChallengeIdAndMemberId(challengeId,
            memberId)).thenReturn(Optional.empty());
        when(pointService.calculateValidPoints(memberId)).thenReturn(
            50); // 부족한 포인트 (joinPoint는 100)

        // when & then
        assertThatThrownBy(() -> challengeService.joinChallenge(challengeId, memberId))
            .isInstanceOf(BaseException.class)
            .hasFieldOrPropertyWithValue("baseResponseCode", BaseResponseCode.NOT_ENOUGH_POINT);

        verify(challengeRepository).findById(challengeId);
        verify(memberRepository).findById(memberId);
        verify(challengeMemberRepository).findByChallengeIdAndMemberId(challengeId, memberId);
        verify(pointService).calculateValidPoints(memberId);
        verifyNoMoreInteractions(pointLogRepository);
    }

    // 테스트 데이터 생성 헬퍼 메서드들
    private CreateChallengeReqDTO createTestChallengeReqDTO() {
        return new CreateChallengeReqDTO() {
            @Override
            public String getTitle() {
                return "테스트 챌린지";
            }

            @Override
            public LocalDate getStartDate() {
                return LocalDate.now().plusDays(1);
            }

            @Override
            public ChallengePeriod getPeriod() {
                return ChallengePeriod.WEEK;
            }

            @Override
            public Integer getTotalGoalDay() {
                return 5;
            }

            @Override
            public Category getCategory() {
                return Category.PLOGGING;
            }

            @Override
            public String getDetails() {
                return "테스트 챌린지 설명";
            }

            @Override
            public List<String> getNotes() {
                return Arrays.asList("주의사항1", "주의사항2");
            }

            @Override
            public Integer getJoinPoint() {
                return 100;
            }
        };
    }

    private CreateChallengeReqDTO createTestChallengeReqDTOWithStartDate(LocalDate startDate) {
        return new CreateChallengeReqDTO() {
            @Override
            public String getTitle() {
                return "테스트 챌린지";
            }

            @Override
            public LocalDate getStartDate() {
                return startDate;
            }

            @Override
            public ChallengePeriod getPeriod() {
                return ChallengePeriod.WEEK;
            }

            @Override
            public Integer getTotalGoalDay() {
                return 5;
            }

            @Override
            public Category getCategory() {
                return Category.PLOGGING;
            }

            @Override
            public String getDetails() {
                return "테스트 챌린지 설명";
            }

            @Override
            public List<String> getNotes() {
                return Arrays.asList("주의사항1", "주의사항2");
            }

            @Override
            public Integer getJoinPoint() {
                return 100;
            }
        };
    }
}
