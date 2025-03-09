package site.beilsang.beilsang_server_v2.domain.challenge.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.ChallengeAssembler;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.ChallengeNote;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeNoteRepository;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeRepository;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;
import site.beilsang.beilsang_server_v2.domain.point.repository.PointLogRepository;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;
import site.beilsang.beilsang_server_v2.global.enums.PointName;
import site.beilsang.beilsang_server_v2.global.enums.PointStatus;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeServiceImpl implements ChallengeService {

    private final MemberRepository memberRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final ChallengeNoteRepository challengeNoteRepository;
    private final PointLogRepository pointLogRepository;

    @Override
    public ChallengeResDTO createChallenge(Long memberId, CreateChallengeReqDTO createChallengeReqDTO,
                                           MultipartFile mainImage, MultipartFile certImage) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER)
        );

        // 멤버 포인트 부족 시 예외 처리
        int joinPoint = createChallengeReqDTO.getJoinPoint();
        if (member.getPoint() < joinPoint) {
            throw new BaseException(BaseResponseCode.NOT_ENOUGH_POINT);
        }
        pointLogRepository.save(PointLog.builder()
                .pointName(PointName.JOIN_CHALLENGE)
                .status(PointStatus.USE)
                .value(joinPoint)
                .member(member)
                .build());
        member.subPoint(joinPoint); // 포인트 차감

        // S3 이용 챌린지 이미지 저장 로직
        String mainImageUrl = null;
        String certImageUrl = null;

        // 챌린지 생성
        Challenge challenge = challengeRepository.save(
                ChallengeAssembler.toEntity(createChallengeReqDTO, mainImageUrl, certImageUrl)
        );

        // ChallengeNote 생성
        challengeNoteRepository.saveAll(createChallengeReqDTO.getNotes().stream()
                .map(note -> ChallengeNote.builder()
                        .note(note)
                        .challenge(challenge)
                        .build())
                .toList());

        // ChallengeStatus 상태 결정
        ChallengeStatus challengeStatus = ChallengeStatus.ONGOING;
        if (createChallengeReqDTO.getStartDate().isAfter(LocalDate.now())) {
            challengeStatus = ChallengeStatus.NOT_YET;
        }

        // ChallengeMember 생성
        challengeMemberRepository.save(ChallengeMember.builder()
                .isHost(true)
                .successDays(0)
                .challengeStatus(challengeStatus)
                .isFeedUpload(false)
                .member(member)
                .challenge(challenge)
                .build());

        return ChallengeAssembler.toChallengeResDTO(challenge);
    }
}
