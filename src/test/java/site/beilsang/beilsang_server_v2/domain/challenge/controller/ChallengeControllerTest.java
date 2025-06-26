package site.beilsang.beilsang_server_v2.domain.challenge.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.service.ChallengeService;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import site.beilsang.beilsang_server_v2.global.enums.ChallengePeriod;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChallengeController 단위테스트 (Simple)")
class ChallengeControllerTest {

    @Mock
    private ChallengeService challengeService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ChallengeController challengeController;

    private CreateChallengeReqDTO testCreateChallengeReqDTO;
    private ChallengeResDTO testChallengeResDTO;
    private MockMultipartFile testMainImage;
    private MockMultipartFile testCertImage;

    @BeforeEach
    void setUp() {
        // 테스트용 CreateChallengeReqDTO 준비
        testCreateChallengeReqDTO = createTestChallengeReqDTO();

        // 테스트용 ChallengeResDTO 준비
        testChallengeResDTO = ChallengeResDTO.builder()
                .challengeId(1L)
                .title("테스트 챌린지")
                .category(Category.PLOGGING)
                .startDate(LocalDate.now().plusDays(1))
                .finishDate(LocalDate.now().plusDays(8))
                .joinPoint(100)
                .period(ChallengePeriod.WEEK)
                .totalGoalDay(5)
                .details("테스트 챌린지 설명")
                .challengeNotes(Arrays.asList("주의사항1", "주의사항2"))
                .attendeeCount(1)
                .countLikes(0)
                .collectedPoint(100)
                .build();

        // 테스트용 MultipartFile 준비
        testMainImage = new MockMultipartFile(
                "mainImage",
                "main.jpg",
                "image/jpeg",
                "main image content".getBytes());

        testCertImage = new MockMultipartFile(
                "certImage",
                "cert.jpg",
                "image/jpeg",
                "cert image content".getBytes());
    }

    @Test
    @DisplayName("챌린지 생성 성공 - 정상적인 요청")
    void createChallenge_Success() {
        // given
        Long memberId = 1L;
        when(authentication.getPrincipal()).thenReturn(memberId);
        when(challengeService.createChallenge(eq(memberId), any(CreateChallengeReqDTO.class),
                any(), any())).thenReturn(testChallengeResDTO);

        // when
        BaseResponse<ChallengeResDTO> response = challengeController.createChallenge(
                authentication, testCreateChallengeReqDTO, testMainImage, testCertImage);

        // then
        assertThat(response).isNotNull();
        assertThat(response.statusCode).isEqualTo(200);
        assertThat(response.code).isEqualTo("S0001");
        assertThat(response.message).isEqualTo("요청에 성공했습니다");
        assertThat(response.data).isNotNull();
        assertThat(response.data).isEqualTo(testChallengeResDTO);

        // 서비스 메서드 호출 검증
        verify(authentication).getPrincipal();
        verify(challengeService).createChallenge(eq(memberId), eq(testCreateChallengeReqDTO),
                eq(testMainImage), eq(testCertImage));
    }

    @Test
    @DisplayName("챌린지 생성 실패 - 존재하지 않는 멤버")
    void createChallenge_MemberNotFound() {
        // given
        Long memberId = 999L;
        when(authentication.getPrincipal()).thenReturn(memberId);
        when(challengeService.createChallenge(eq(memberId), any(CreateChallengeReqDTO.class),
                any(), any())).thenThrow(new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        // when & then
        assertThatThrownBy(() -> challengeController.createChallenge(
                authentication, testCreateChallengeReqDTO, testMainImage, testCertImage))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("baseResponseCode", BaseResponseCode.NOT_FOUND_MEMBER);

        // 서비스 메서드 호출 검증
        verify(authentication).getPrincipal();
        verify(challengeService).createChallenge(eq(memberId), eq(testCreateChallengeReqDTO),
                eq(testMainImage), eq(testCertImage));
    }

    @Test
    @DisplayName("챌린지 생성 실패 - 포인트 부족")
    void createChallenge_NotEnoughPoint() {
        // given
        Long memberId = 1L;
        when(authentication.getPrincipal()).thenReturn(memberId);
        when(challengeService.createChallenge(eq(memberId), any(CreateChallengeReqDTO.class),
                any(), any())).thenThrow(new BaseException(BaseResponseCode.NOT_ENOUGH_POINT));

        // when & then
        assertThatThrownBy(() -> challengeController.createChallenge(
                authentication, testCreateChallengeReqDTO, testMainImage, testCertImage))
                .isInstanceOf(BaseException.class)
                .hasFieldOrPropertyWithValue("baseResponseCode", BaseResponseCode.NOT_ENOUGH_POINT);

        // 서비스 메서드 호출 검증
        verify(authentication).getPrincipal();
        verify(challengeService).createChallenge(eq(memberId), eq(testCreateChallengeReqDTO),
                eq(testMainImage), eq(testCertImage));
    }

    @Test
    @DisplayName("Authentication에서 멤버 ID 정상 추출")
    void extractMemberIdFromAuthentication() {
        // given
        Long expectedMemberId = 123L;
        when(authentication.getPrincipal()).thenReturn(expectedMemberId);
        when(challengeService.createChallenge(eq(expectedMemberId), any(CreateChallengeReqDTO.class),
                any(), any())).thenReturn(testChallengeResDTO);

        // when
        challengeController.createChallenge(authentication, testCreateChallengeReqDTO, testMainImage, testCertImage);

        // then
        verify(authentication).getPrincipal();
        verify(challengeService).createChallenge(eq(expectedMemberId), any(CreateChallengeReqDTO.class),
                any(), any());
    }

    @Test
    @DisplayName("서비스 메서드 파라미터 전달 확인")
    void verifyServiceMethodParameters() {
        // given
        Long memberId = 1L;
        when(authentication.getPrincipal()).thenReturn(memberId);
        when(challengeService.createChallenge(any(), any(), any(), any())).thenReturn(testChallengeResDTO);

        // when
        challengeController.createChallenge(authentication, testCreateChallengeReqDTO, testMainImage, testCertImage);

        // then
        verify(challengeService).createChallenge(
                eq(memberId),
                eq(testCreateChallengeReqDTO),
                eq(testMainImage),
                eq(testCertImage));
    }

    @Test
    @DisplayName("BaseResponse 래핑 확인")
    void verifyBaseResponseWrapping() {
        // given
        Long memberId = 1L;
        when(authentication.getPrincipal()).thenReturn(memberId);
        when(challengeService.createChallenge(any(), any(), any(), any())).thenReturn(testChallengeResDTO);

        // when
        BaseResponse<ChallengeResDTO> response = challengeController.createChallenge(
                authentication, testCreateChallengeReqDTO, testMainImage, testCertImage);

        // then
        assertThat(response).isNotNull();
        assertThat(response.statusCode).isEqualTo(BaseResponseCode.SUCCESS.getStatus().value());
        assertThat(response.code).isEqualTo(BaseResponseCode.SUCCESS.getCode());
        assertThat(response.message).isEqualTo(BaseResponseCode.SUCCESS.getMessage());
        assertThat(response.data).isEqualTo(testChallengeResDTO);
    }

    // 테스트 헬퍼 메서드들
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
}