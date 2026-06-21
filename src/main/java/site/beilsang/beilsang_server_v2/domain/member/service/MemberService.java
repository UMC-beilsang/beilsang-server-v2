package site.beilsang.beilsang_server_v2.domain.member.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.feed.repository.FeedRepository;
import site.beilsang.beilsang_server_v2.domain.like.repository.ChallengeLikeRepository;
import site.beilsang.beilsang_server_v2.domain.member.dto.MemberAssembler;
import site.beilsang.beilsang_server_v2.domain.member.dto.req.MemberProfileImageReqDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.req.MemberNicknameReqDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.req.TermsAgreementReqDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.ChallengeCountResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.CheckEnrolledResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.FeedCountResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.LikeCountResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberProfileImageResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberNicknameResDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.point.dto.PointAssembler;
import site.beilsang.beilsang_server_v2.domain.point.dto.res.PointLogListResDTO;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;
import site.beilsang.beilsang_server_v2.domain.point.repository.PointLogRepository;
import site.beilsang.beilsang_server_v2.domain.point.service.PointService;
import site.beilsang.beilsang_server_v2.global.aws.s3.S3Service;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;
import site.beilsang.beilsang_server_v2.global.enums.PointStatus;
import site.beilsang.beilsang_server_v2.global.enums.UploadPath;

/**
 * 회원 관리 및 마이페이지 관련 비즈니스 로직을 처리하는 서비스 회원 프로필 관리, 포인트 내역 조회, 챌린지 참여 확인 등의 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class MemberService {

    private final ChallengeMemberRepository challengeMemberRepository;
    private final ChallengeLikeRepository challengeLikeRepository;
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;
    private final PointLogRepository pointLogRepository;
    private final PointService pointService;
    private final S3Service s3Service;

    @Value("${member.default-profile-image}")
    private String defaultProfileImageUrl;

    public PointLogListResDTO getPointLog(Long memberId) {
        Member member = memberRepository.findById(memberId).
            orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        List<PointLog> pointLogList = pointLogRepository.findAllByMemberIdAndStatusNot(memberId,
            PointStatus.EXPIRE);
        pointService.expirePointsIfNeeded(pointLogList);

        List<PointLog> validPointLogList = pointLogList.stream()
            .filter(pointLog -> pointLog.getStatus() != PointStatus.EXPIRE)
            .toList();
        return PointAssembler.toEntities(validPointLogList, member);
    }

    public MemberNicknameResDTO updateNickname(Long memberId, MemberNicknameReqDTO memberNicknameReqDTO) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        String newNickname = memberNicknameReqDTO.getNickName();

        // 닉네임 중복 체크 (본인 제외)
        if (memberRepository.existsByNickName(newNickname) &&
            !newNickname.equals(member.getNickName())) {
            throw new BaseException(BaseResponseCode.DUPLICATE_NICKNAME);
        }

        member.updateNickname(newNickname);
        memberRepository.save(member);
        return MemberAssembler.toNicknameResDTO(member);
    }

    public void updateProfileImage(Long memberId,
        MemberProfileImageReqDTO memberProfileImageReqDTO) {
        MultipartFile profileImage = memberProfileImageReqDTO.getProfileImage();

        // 파일 누락 또는 빈 파일이면 즉시 실패 처리 (S3 업로드 전에 검증)
        if (profileImage == null || profileImage.isEmpty()) {
            throw new BaseException(BaseResponseCode.INVALID_IMAGE_FILE);
        }

        Member member = memberRepository.findById(memberId).
            orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        // 회원 ID를 서브디렉토리로 사용하여 S3에 업로드 (member/profile/{memberId}/filename)
        String profileUrl = s3Service.uploadFile(
            UploadPath.MEMBER_PROFILE,
            memberId.toString(),
            profileImage
        );
        member.updateProfileImageUrl(profileUrl);
        memberRepository.save(member);
    }

    public CheckEnrolledResDTO checkEnroll(Long memberId, Long challengeId) {

        List<Long> enrolledChallengeIds = challengeMemberRepository.findAllByMemberId(memberId)
            .stream()
            .filter(challengeMember -> challengeMember.getChallenge().getFinishDate()
                .isAfter(LocalDate.now())) // 아직 끝나지 않은 챌린지만
            .map(challengeMember -> challengeMember.getChallenge().getId())
            .toList();

        Boolean isEnrolled = enrolledChallengeIds.contains(challengeId);
        return MemberAssembler.toCheckEnrolledDTO(isEnrolled, enrolledChallengeIds);
    }

    public void agreeToTerms(Long memberId, TermsAgreementReqDTO termsAgreementReqDTO) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        if (termsAgreementReqDTO.getAgreed()) {
            member.agreeToTerms();
            memberRepository.save(member);
        }
    }

    public FeedCountResDTO getFeedCount(Long memberId) {
        memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));
        Long countFeed = feedRepository.countByMemberId(memberId);
        return MemberAssembler.toFeedCountResDTO(countFeed);
    }

    public ChallengeCountResDTO getChallengeCount(Long memberId) {
        memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));
        Long countChallenge = challengeMemberRepository.countByMemberId(memberId);
        Long countSuccessChallenge = challengeMemberRepository.countByMemberIdAndChallengeMemberStatus(
            memberId, ChallengeMemberStatus.SUCCESS);
        Long countFailedChallenge = challengeMemberRepository.countByMemberIdAndChallengeMemberStatus(
            memberId, ChallengeMemberStatus.FAIL);
        return MemberAssembler.toChallengeCountResDTO(countChallenge, countSuccessChallenge,
            countFailedChallenge);
    }

    public LikeCountResDTO getLikeCount(Long memberId) {
        memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));
        Long countLike = challengeLikeRepository.countByMemberId(memberId);
        return MemberAssembler.toLikeCountResDTO(countLike);
    }

    public MemberNicknameResDTO getNickname(Long challengerId) {
        Member member = memberRepository.findById(challengerId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));
        return MemberAssembler.toNicknameResDTO(member);
    }

    public MemberProfileImageResDTO getProfileImage(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        // 이번 배포 이전에 가입한 회원은 profileUrl이 null 또는 ""일 수 있으므로 기본 이미지로 대체
        if (member.getProfileUrl() == null || member.getProfileUrl().isBlank()) {
            member.updateProfileImageUrl(defaultProfileImageUrl);
            memberRepository.save(member);
        }

        return MemberAssembler.toProfileImageResDTO(member);
    }
}
