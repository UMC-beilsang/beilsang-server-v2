package site.beilsang.beilsang_server_v2.domain.challenge.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.ChallengeAssembler;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.CreateChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeRepository;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final MemberRepository memberRepository;
    private final ChallengeRepository challengeRepository;

    @Override
    public CreateChallengeResDTO createChallenge(Long memberId, CreateChallengeReqDTO createChallengeReqDTO, MultipartFile mainImage, MultipartFile certImage) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER)
        );

        // S3 이용 챌린지 이미지 저장 로직
        String mainImageUrl = null;
        String certImageUrl = null;

        // 챌린지 생성
        Challenge challenge = ChallengeAssembler.toEntity(createChallengeReqDTO, mainImageUrl, certImageUrl);

        // ChallengeMember 생성 로직

        return null;
    }
}
