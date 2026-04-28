package site.beilsang.beilsang_server_v2.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import site.beilsang.beilsang_server_v2.domain.feed.repository.FeedRepository;
import site.beilsang.beilsang_server_v2.domain.like.repository.ChallengeLikeRepository;
import site.beilsang.beilsang_server_v2.domain.member.dto.req.MemberProfileImageReqDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberProfileImageResDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.point.repository.PointLogRepository;
import site.beilsang.beilsang_server_v2.domain.point.service.PointService;
import site.beilsang.beilsang_server_v2.global.aws.s3.S3Service;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.enums.UploadPath;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 단위테스트")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ChallengeMemberRepository challengeMemberRepository;
    @Mock
    private ChallengeLikeRepository challengeLikeRepository;
    @Mock
    private FeedRepository feedRepository;
    @Mock
    private PointLogRepository pointLogRepository;
    @Mock
    private PointService pointService;
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private MemberService memberService;

    private static final String DEFAULT_PROFILE_IMAGE_URL = "https://s3.example.com/member/profile/default.png";
    private static final String UPLOADED_PROFILE_IMAGE_URL = "https://s3.example.com/member/profile/1/uuid.png";

    @BeforeEach
    void setUp() {
        // @Value 필드 주입
        ReflectionTestUtils.setField(memberService, "defaultProfileImageUrl", DEFAULT_PROFILE_IMAGE_URL);
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 - 정상적으로 S3에 업로드되고 URL이 저장된다")
    void updateProfileImage_success() {
        // given
        Long memberId = 1L;
        Member member = Member.builder().build();
        MockMultipartFile mockFile = new MockMultipartFile(
            "profileImage", "profile.png", "image/png", "image-data".getBytes()
        );
        MemberProfileImageReqDTO reqDTO = new MemberProfileImageReqDTO(mockFile);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(s3Service.uploadFile(eq(UploadPath.MEMBER_PROFILE), eq("1"), eq(mockFile)))
            .thenReturn(UPLOADED_PROFILE_IMAGE_URL);
        when(memberRepository.save(any())).thenReturn(member);

        // when
        memberService.updateProfileImage(memberId, reqDTO);

        // then
        assertThat(member.getProfileUrl()).isEqualTo(UPLOADED_PROFILE_IMAGE_URL);
        verify(s3Service).uploadFile(UploadPath.MEMBER_PROFILE, "1", mockFile);
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("프로필 이미지 업데이트 - 존재하지 않는 회원이면 NOT_FOUND_MEMBER 예외가 발생한다")
    void updateProfileImage_memberNotFound() {
        // given
        Long memberId = 999L;
        MockMultipartFile mockFile = new MockMultipartFile(
            "profileImage", "profile.png", "image/png", "image-data".getBytes()
        );
        MemberProfileImageReqDTO reqDTO = new MemberProfileImageReqDTO(mockFile);

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.updateProfileImage(memberId, reqDTO))
            .isInstanceOf(BaseException.class)
            .satisfies(e -> assertThat(((BaseException) e).getBaseResponseCode())
                .isEqualTo(BaseResponseCode.NOT_FOUND_MEMBER));

        verify(s3Service, never()).uploadFile(any(), any(), any());
    }

    @Test
    @DisplayName("프로필 이미지 조회 - profileUrl이 있는 회원은 해당 URL을 반환한다")
    void getProfileImage_hasProfileUrl() {
        // given
        Long memberId = 1L;
        Member member = Member.builder()
            .profileUrl(UPLOADED_PROFILE_IMAGE_URL)
            .build();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // when
        MemberProfileImageResDTO result = memberService.getProfileImage(memberId);

        // then
        assertThat(result.getProfileUrl()).isEqualTo(UPLOADED_PROFILE_IMAGE_URL);
        verify(memberRepository, never()).save(any());
    }

    @Test
    @DisplayName("프로필 이미지 조회 - profileUrl이 null인 기존 회원은 기본 이미지 URL을 반환하고 저장한다")
    void getProfileImage_nullProfileUrl_returnsDefaultImage() {
        // given
        Long memberId = 1L;
        Member member = Member.builder().build(); // profileUrl = null

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(memberRepository.save(any())).thenReturn(member);

        // when
        MemberProfileImageResDTO result = memberService.getProfileImage(memberId);

        // then
        assertThat(result.getProfileUrl()).isEqualTo(DEFAULT_PROFILE_IMAGE_URL);
        verify(memberRepository).save(member);
    }

    @Test
    @DisplayName("프로필 이미지 조회 - 존재하지 않는 회원이면 NOT_FOUND_MEMBER 예외가 발생한다")
    void getProfileImage_memberNotFound() {
        // given
        Long memberId = 999L;
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getProfileImage(memberId))
            .isInstanceOf(BaseException.class)
            .satisfies(e -> assertThat(((BaseException) e).getBaseResponseCode())
                .isEqualTo(BaseResponseCode.NOT_FOUND_MEMBER));
    }
}
