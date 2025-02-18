package site.beilsang.beilsang_server_v2.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.feed.repository.FeedRepository;
import site.beilsang.beilsang_server_v2.domain.like.repository.ChallengeLikeRepository;
import site.beilsang.beilsang_server_v2.domain.member.dto.MemberAssembler;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.MyPageResDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.point.dto.PointAssembler;
import site.beilsang.beilsang_server_v2.domain.point.dto.res.PointLogListResDTO;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;
import site.beilsang.beilsang_server_v2.domain.point.repository.PointLogRepository;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final ChallengeMemberRepository challengeMemberRepository;
    private final ChallengeLikeRepository challengeLikeRepository;
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;
    private final PointLogRepository pointLogRepository;

    /**
     * mypage에 필요한 모든 값들을 return
     * 피드 개수, 달성한 챌린지 개수, 다짐, 챌린지 개수, 실패한 챌린지 개수, 찜 개수, 보유 포인트
     *
     * @param memberId
     * @return
     */
    public MyPageResDTO getMyPage(Long memberId) {
        Member member = memberRepository.findById(memberId).
                orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));
        List<ChallengeMember> challengeMembers = challengeMemberRepository.findAllByMemberId(memberId);
        List<Long> challengeMemberIds = challengeMembers.stream().map(ChallengeMember::getId).toList();

        //피드 개수
        Long countFeed = feedRepository.countByChallengeMember_IdIn(challengeMemberIds);

        //달성한 챌린지 개수
        Long countSuccessChallenge = challengeMemberRepository.countByMemberIdAndChallengeStatus(memberId, ChallengeStatus.SUCCESS);

        // 챌린지 개수 : 멤버 아이디로 챌린지멤버 테이블 카운트
        Long countChallenge = challengeMemberRepository.countByMemberId(memberId);

        // 실패한 챌린지 개수
        Long countFailedChallenge = challengeMemberRepository.countByMemberIdAndChallengeStatus(memberId, ChallengeStatus.FAIL);

        // 찜 개수 : 회원 아이디로 챌린지라이크 테이블 접근해서 카운트
        Long countlike = challengeLikeRepository.countByMemberId(memberId);

        //최근 4개 피드
        List<Feed> feedList = feedRepository.findTop4ByChallengeMember_IdInOrderByCreatedAtDesc(challengeMemberIds);
        return MemberAssembler.toMyPageResDTO(member, feedList, countFeed, countSuccessChallenge, countChallenge, countFailedChallenge, countlike);
    }

    public PointLogListResDTO getPointLog(Long memberId) {
        Member member = memberRepository.findById(memberId).
                orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        List<PointLog> pointLogList = pointLogRepository.findAllByMemberId(memberId);
        return PointAssembler.toEntities(pointLogList, member);
    }
}
