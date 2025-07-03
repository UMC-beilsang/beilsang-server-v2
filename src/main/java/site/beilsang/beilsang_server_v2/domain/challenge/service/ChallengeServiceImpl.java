package site.beilsang.beilsang_server_v2.domain.challenge.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.ChallengeAssembler;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.ChallengeNote;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.ChallengeInfoImage;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.ChallengeCertImage;
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
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;
import site.beilsang.beilsang_server_v2.global.enums.PointName;
import site.beilsang.beilsang_server_v2.global.enums.PointStatus;
import site.beilsang.beilsang_server_v2.global.enums.UploadPath;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListRequestDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeListResponseDTO;
import site.beilsang.beilsang_server_v2.global.common.PageResponseDTO;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeServiceImpl implements ChallengeService {

    private static final int MAX_INFO_IMAGE = 5;
    private static final int MAX_CERT_IMAGE = 4;

    private final MemberRepository memberRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final ChallengeNoteRepository challengeNoteRepository;
    private final PointLogRepository pointLogRepository;
    private final S3Service s3Service;

    @Override
    public ChallengeResDTO createChallenge(Long memberId, CreateChallengeReqDTO createChallengeReqDTO,
            List<MultipartFile> infoImages, List<MultipartFile> certImages) {
        // 이미지 파일 검증
        validateImages(infoImages, certImages);

        // 목표 일수가 전체 기간을 초과하지 않는지 검증
        if (createChallengeReqDTO.getTotalGoalDay() > createChallengeReqDTO.getPeriod().getDays()) {
            throw new BaseException(BaseResponseCode.INVALID_CHALLENGE_PERIOD);
        }

        // 시작 날짜가 오늘 이후인지 검증 (오늘은 허용)
        if (createChallengeReqDTO.getStartDate().isBefore(LocalDate.now())) {
            throw new BaseException(BaseResponseCode.INVALID_START_DATE);
        }

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

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

        // 챌린지 생성
        Challenge challenge = challengeRepository.save(
                ChallengeAssembler.toEntity(createChallengeReqDTO));

        // 챌린지 정보 이미지들 업로드 및 저장
        saveInfoImages(challenge, infoImages);

        // 챌린지 인증 이미지들 업로드 및 저장
        saveCertImages(challenge, certImages);

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

    private void validateImages(List<MultipartFile> infoImages, List<MultipartFile> certImages) {
        // 정보 이미지 검증
        if (infoImages == null || infoImages.isEmpty()) {
            throw new BaseException(BaseResponseCode.INVALID_IMAGE_FILE);
        }
        if (infoImages.size() > MAX_INFO_IMAGE) { // 최대 10장 제한
            throw new BaseException(BaseResponseCode.INVALID_IMAGE_FILE); // TODO: 적절한 에러 코드로 변경
        }

        // 인증 이미지 검증
        if (certImages == null || certImages.isEmpty()) {
            throw new BaseException(BaseResponseCode.INVALID_IMAGE_FILE);
        }
        if (certImages.size() > MAX_CERT_IMAGE) { // 최대 5장 제한
            throw new BaseException(BaseResponseCode.INVALID_IMAGE_FILE); // TODO: 적절한 에러 코드로 변경
        }

        // 각 파일 유효성 검증
        validateEachFile(infoImages);
        validateEachFile(certImages);
    }

    private void validateEachFile(List<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new BaseException(BaseResponseCode.INVALID_IMAGE_FILE);
            }
        }
    }

    private void saveInfoImages(Challenge challenge, List<MultipartFile> infoImages) {
        for (int i = 0; i < infoImages.size(); i++) {
            String imageUrl = s3Service.uploadFile(UploadPath.CHALLENGE_MAIN, infoImages.get(i));
            ChallengeInfoImage infoImage = ChallengeInfoImage.builder()
                    .imageUrl(imageUrl)
                    .imageOrder(i + 1)
                    .challenge(challenge)
                    .build();
            challenge.getInfoImages().add(infoImage);
        }
    }

    private void saveCertImages(Challenge challenge, List<MultipartFile> certImages) {
        for (int i = 0; i < certImages.size(); i++) {
            String imageUrl = s3Service.uploadFile(UploadPath.CHALLENGE_CERT, certImages.get(i));
            ChallengeCertImage certImage = ChallengeCertImage.builder()
                    .imageUrl(imageUrl)
                    .imageOrder(i + 1)
                    .challenge(challenge)
                    .build();
            challenge.getCertImages().add(certImage);
        }
    }

    @Override
    public PageResponseDTO<ChallengeListResponseDTO> getChallengeList(ChallengeListRequestDTO requestDTO) {
        // TODO: 구현 예정
        return null;
    }
}
