package site.beilsang.beilsang_server_v2.domain.member.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.feed.repository.FeedRepository;
import site.beilsang.beilsang_server_v2.domain.like.repository.ChallengeLikeRepository;
import site.beilsang.beilsang_server_v2.domain.member.dto.MemberAssembler;
import site.beilsang.beilsang_server_v2.domain.member.dto.req.MemberProfileImageReqDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.req.MemberProfileReqDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.CheckEnrolledResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MemberProfileResDTO;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MyPageResDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.point.dto.PointAssembler;
import site.beilsang.beilsang_server_v2.domain.point.dto.res.PointLogListResDTO;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;
import site.beilsang.beilsang_server_v2.domain.point.repository.PointLogRepository;
import site.beilsang.beilsang_server_v2.domain.point.service.PointService;
import site.beilsang.beilsang_server_v2.domain.uuid.entity.Uuid;
import site.beilsang.beilsang_server_v2.domain.uuid.repository.UuidRepository;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;
import site.beilsang.beilsang_server_v2.global.enums.PointStatus;

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
    private final UuidRepository uuidRepository;

    /**
     * mypage에 필요한 모든 값들을 return 피드 개수, 달성한 챌린지 개수, 다짐, 챌린지 개수, 실패한 챌린지 개수, 찜 개수, 보유 포인트
     *
     * @param memberId
     * @return
     */
    public MyPageResDTO getMyPage(Long memberId) {
        Member member = memberRepository.findById(memberId).
            orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));
        List<ChallengeMember> challengeMembers = challengeMemberRepository.findAllByMemberId(
            memberId);
        List<Long> challengeMemberIds = challengeMembers.stream().map(ChallengeMember::getId)
            .toList();

        //피드 개수
        Long countFeed = feedRepository.countByChallengeMember_IdIn(challengeMemberIds);

        //달성한 챌린지 개수
        Long countSuccessChallenge = challengeMemberRepository.countByMemberIdAndChallengeMemberStatus(
            memberId, ChallengeMemberStatus.SUCCESS);

        // 챌린지 개수 : 멤버 아이디로 챌린지멤버 테이블 카운트
        Long countChallenge = challengeMemberRepository.countByMemberId(memberId);

        // 실패한 챌린지 개수
        Long countFailedChallenge = challengeMemberRepository.countByMemberIdAndChallengeMemberStatus(
            memberId, ChallengeMemberStatus.FAIL);

        // 찜 개수 : 회원 아이디로 챌린지라이크 테이블 접근해서 카운트
        Long countlike = challengeLikeRepository.countByMemberId(memberId);

        //최근 4개 피드
        List<Feed> feedList = feedRepository.findTop4ByChallengeMember_IdInOrderByCreatedAtDesc(
            challengeMemberIds);
        return MemberAssembler.toMyPageResDTO(member, feedList, countFeed, countSuccessChallenge,
            countChallenge, countFailedChallenge, countlike);
    }

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

    public MemberProfileResDTO updateProfile(Long memberId,
        MemberProfileReqDTO memberProfileReqDTO) {
        Member member = memberRepository.findById(memberId).
            orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));
        member.updateProfile(memberProfileReqDTO);
        memberRepository.save(member);
        return MemberAssembler.toEntity(member);
    }

    public Void updateProfileImage(Long memberId,
        MemberProfileImageReqDTO memberProfileImageReqDTO) {
        Member member = memberRepository.findById(memberId).
            orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        Uuid feedUuid = uuidRepository.save(
            Uuid.builder().uuid(UUID.randomUUID().toString()).build());
        //TODO - S3 설정
//        String feedUrl = s3Manager.uploadFile(s3Manager.generateFeedKeyName(feedUuid), profileImageDTO.getProfileImage());
//        member.updateProfileImageUrl(feedUrl);
        return null;
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
}
