package site.beilsang.beilsang_server_v2.domain.challenge.service;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.ChallengeAssembler;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ClosedChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.OpenChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.SearchClosedChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.SearchOpenChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeDetailResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeListResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.JoinChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.ChallengeCertImage;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.ChallengeInfoImage;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.ChallengeNote;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeNoteRepository;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeRepository;
import site.beilsang.beilsang_server_v2.domain.like.entity.ChallengeLike;
import site.beilsang.beilsang_server_v2.domain.like.repository.ChallengeLikeRepository;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.member.repository.ChallengeMemberRepository;
import site.beilsang.beilsang_server_v2.domain.member.repository.MemberRepository;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;
import site.beilsang.beilsang_server_v2.domain.point.repository.PointLogRepository;
import site.beilsang.beilsang_server_v2.domain.point.service.PointService;
import site.beilsang.beilsang_server_v2.global.aws.s3.S3Service;
import site.beilsang.beilsang_server_v2.global.common.PageResponseDTO;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseException;
import site.beilsang.beilsang_server_v2.global.common.exception.BaseResponseCode;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeMemberStatus;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;
import site.beilsang.beilsang_server_v2.global.enums.PointName;
import site.beilsang.beilsang_server_v2.global.enums.PointStatus;
import site.beilsang.beilsang_server_v2.global.enums.UploadPath;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeServiceImpl implements ChallengeService {

    private static final int MAX_INFO_IMAGE = 5;
    private static final int MAX_CERT_IMAGE = 4;
    private static final int POINT_EXPIRATION_YEARS = 1;
    private static final int INIT_SUCCESS_DAYS = 0;

    private final MemberRepository memberRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeMemberRepository challengeMemberRepository;
    private final ChallengeNoteRepository challengeNoteRepository;
    private final ChallengeLikeRepository challengeLikeRepository;
    private final PointLogRepository pointLogRepository;
    private final PointService pointService;
    private final S3Service s3Service;
    private final ChallengeAssembler challengeAssembler;

    @Override
    public ChallengeResDTO createChallenge(Long memberId,
        CreateChallengeReqDTO createChallengeReqDTO,
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
        int validPoints = pointService.calculateValidPoints(memberId);
        if (validPoints < joinPoint) {
            throw new BaseException(BaseResponseCode.NOT_ENOUGH_POINT);
        }
        // 챌린지 생성
        Challenge challenge = challengeRepository.save(
            ChallengeAssembler.toEntity(createChallengeReqDTO));

        pointLogRepository.save(PointLog.builder()
            .pointName(PointName.JOIN_CHALLENGE)
            .status(PointStatus.USE)
            .points(joinPoint)
            .expirationDate(LocalDateTime.now().plusYears(POINT_EXPIRATION_YEARS))
            .member(member)
            .challenge(challenge)
            .build());
        member.subPoint(joinPoint); // 포인트 차감

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

        // ChallengeMemberStatus 상태 결정 - Challenge의 현재 상태를 기반으로 결정
        ChallengeMemberStatus challengeMemberStatus =
            challenge.getStatus() == ChallengeStatus.NOT_YET ?
                ChallengeMemberStatus.NOT_YET : ChallengeMemberStatus.ONGOING;

        // ChallengeMember 생성
        challengeMemberRepository.save(ChallengeMember.builder()
            .isHost(true)
            .successDays(INIT_SUCCESS_DAYS)
            .challengeMemberStatus(challengeMemberStatus)
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

    @Deprecated
    @Override
    public PageResponseDTO<ChallengeListResDTO> getChallengeList(ChallengeListReqDTO requestDTO) {
        Pageable pageable = PageRequest.of(
            requestDTO.getPage() != null ? requestDTO.getPage() : 0,
            requestDTO.getSize() != null ? requestDTO.getSize() : 10);
        Page<Challenge> page = challengeRepository.findChallenges(requestDTO, pageable);
        List<ChallengeListResDTO> content = page.getContent().stream()
            .map(ChallengeAssembler::toChallengeListResDTO)
            .collect(Collectors.toList());
        return PageResponseDTO.<ChallengeListResDTO>builder()
            .content(content)
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .build();
    }

    /**
     * 모집중인 챌린지 목록을 조회합니다.
     * - 시작일이 오늘 이후인 챌린지를 대상으로 조회
     * - 카테고리 필터링 및 정렬 기능 제공 (마감 임박순/최신순)
     */
    @Override
    public PageResponseDTO<ChallengeListResDTO> getOpenChallengeList(
        OpenChallengeListReqDTO requestDTO) {
        Pageable pageable = PageRequest.of(
            requestDTO.getPage() != null ? requestDTO.getPage() : 0,
            requestDTO.getSize() != null ? requestDTO.getSize() : 10);

        Page<Challenge> page = challengeRepository.findOpenChallenges(requestDTO, pageable);

        List<ChallengeListResDTO> content = page.getContent().stream()
            .map(ChallengeAssembler::toChallengeListResDTO)
            .collect(Collectors.toList());

        return PageResponseDTO.<ChallengeListResDTO>builder()
            .content(content)
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .build();
    }

    /**
     * 모집마감된 챌린지 목록을 조회합니다.
     * - 시작일이 오늘 이전인 챌린지를 대상으로 조회
     * - 최근 마감순(startDate DESC)으로 정렬
     */
    @Override
    public PageResponseDTO<ChallengeListResDTO> getClosedChallengeList(
        ClosedChallengeListReqDTO requestDTO) {
        Pageable pageable = PageRequest.of(
            requestDTO.getPage() != null ? requestDTO.getPage() : 0,
            requestDTO.getSize() != null ? requestDTO.getSize() : 10);

        Page<Challenge> page = challengeRepository.findClosedChallenges(requestDTO, pageable);

        List<ChallengeListResDTO> content = page.getContent().stream()
            .map(ChallengeAssembler::toChallengeListResDTO)
            .collect(Collectors.toList());

        return PageResponseDTO.<ChallengeListResDTO>builder()
            .content(content)
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .build();
    }

    @Override
    public ChallengeDetailResDTO getChallengeDetail(Long challengeId, Long memberId) {
        Challenge challenge = challengeRepository.getChallengeById(challengeId);
        if (challenge == null) {
            throw new BaseException(BaseResponseCode.NOT_FOUND_CHALLENGE);
        }

        Optional<ChallengeMember> challengeMemberOpt = challengeMemberRepository.findByChallengeIdAndMemberId(
            challengeId, memberId);

        // 참여 가능 여부 판단
        boolean isJoinable =
            challengeMemberOpt.isEmpty() && challenge.getStatus() != ChallengeStatus.END;

        // 찜 여부 확인
        boolean isLiked = challengeLikeRepository.existsByMemberIdAndChallengeId(memberId,
            challengeId);

        // 챌린지 상태
        ChallengeMemberStatus status = challengeMemberOpt
            .map(ChallengeMember::getChallengeMemberStatus)
            .orElse(ChallengeMemberStatus.NOT_JOINED);

        // 챌린지 진행률 계산
        Float progress = challengeMemberOpt
            .filter(member -> status == ChallengeMemberStatus.ONGOING)
            .map(member -> (float) member.getSuccessDays() / challenge.getTotalGoalDay())
            .orElse(null);

        // 포인트 정보 계산
        Integer usedPoint = null;
        Integer earnedPoint = null;

        if (challengeMemberOpt.isPresent() && (status == ChallengeMemberStatus.SUCCESS
            || status == ChallengeMemberStatus.FAIL)) {
            List<PointLog> pointLogs = pointLogRepository.findByMemberIdAndChallengeId(memberId,
                challengeId);
            usedPoint = calculatePointSum(pointLogs, PointStatus.USE);
            earnedPoint = calculatePointSum(pointLogs, PointStatus.EARN);
        }

        return ChallengeAssembler.toChallengeDetailResDTO(
            challenge, isJoinable, isLiked, status, progress, usedPoint, earnedPoint
        );
    }

    @Override
    public JoinChallengeResDTO joinChallenge(Long challengeId, Long memberId) {
        // 챌린지 조회
        Challenge challenge = challengeRepository.findById(challengeId).orElseThrow(
            () -> new BaseException(BaseResponseCode.NOT_FOUND_CHALLENGE));

        // 멤버 조회
        Member member = memberRepository.findById(memberId).orElseThrow(
            () -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        // 이미 참여한 챌린지인지 확인
        if (challengeMemberRepository.findByChallengeIdAndMemberId(challengeId, memberId)
            .isPresent()) {
            throw new BaseException(BaseResponseCode.ALREADY_JOINED_CHALLENGE);
        }

        // 종료된 챌린지인지 확인
        if (challenge.getStatus() == ChallengeStatus.END) {
            throw new BaseException(BaseResponseCode.CHALLENGE_ENDED);
        }

        // 참여 포인트 확인 및 차감
        int joinPoint = challenge.getJoinPoint();
        int validPoints = pointService.calculateValidPoints(memberId);
        if (validPoints < joinPoint) {
            throw new BaseException(BaseResponseCode.NOT_ENOUGH_POINT);
        }

        // 포인트 로그 생성
        pointLogRepository.save(PointLog.builder()

            .pointName(PointName.JOIN_CHALLENGE)
            .status(PointStatus.USE)
            .points(joinPoint)
            .expirationDate(LocalDateTime.now().plusYears(POINT_EXPIRATION_YEARS))
            .member(member)
            .challenge(challenge)
            .build());

        // 멤버 포인트 차감
        member.subPoint(joinPoint);

        // 챌린지 멤버 상태 결정
        ChallengeMemberStatus challengeMemberStatus =
            challenge.getStatus() == ChallengeStatus.NOT_YET ?
                ChallengeMemberStatus.NOT_YET : ChallengeMemberStatus.ONGOING;

        // 챌린지 멤버 생성
        challengeMemberRepository.save(ChallengeMember.builder()
            .isHost(false)
            .successDays(INIT_SUCCESS_DAYS)
            .challengeMemberStatus(challengeMemberStatus)
            .isFeedUpload(false)
            .member(member)
            .challenge(challenge)
            .build());

        // 챌린지 참여자 수 증가
        challenge.incrementAttendeeCount();

        // 참여 후 남은 포인트 계산
        Integer remainingPoint = pointService.calculateValidPoints(memberId);

        // 응답 DTO 생성 및 반환
        return ChallengeAssembler.toJoinChallengeResDTO(challenge, memberId, remainingPoint);
    }

    private Integer calculatePointSum(List<PointLog> pointLogs, PointStatus targetStatus) {
        return pointLogs.stream()
            .filter(pointLog -> pointLog.getStatus() == targetStatus)
            .mapToInt(PointLog::getPoints)
            .sum();
    }

    /**
     * 모집 마감 챌린지 검색 - 시작일이 오늘 이전인 챌린지를 대상으로 검색 - 오늘 날짜에 가까운 순으로 정렬 (startDate 내림차순)
     */
    @Override
    public PageResponseDTO<ChallengeListResDTO> searchClosedChallenges(
        SearchClosedChallengeReqDTO requestDTO) {
        Pageable pageable = PageRequest.of(
            requestDTO.getPage() != null ? requestDTO.getPage() : 0,
            requestDTO.getSize() != null ? requestDTO.getSize() : 10);

        Page<Challenge> page = challengeRepository.searchClosedChallenges(
            requestDTO.getKeyword(), pageable);

        List<ChallengeListResDTO> content = page.getContent().stream()
            .map(ChallengeAssembler::toChallengeListResDTO)
            .collect(Collectors.toList());

        return PageResponseDTO.<ChallengeListResDTO>builder()
            .content(content)
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .build();
    }

    /**
     * 모집 중인 챌린지 검색 - 시작일이 오늘 이후인 챌린지를 대상으로 검색 - 정렬: 마감 임박순(DEADLINE_SOON) 또는 최신순(NEWEST)
     */
    @Override
    public PageResponseDTO<ChallengeListResDTO> searchOpenChallenges(
        SearchOpenChallengeReqDTO requestDTO) {
        Pageable pageable = PageRequest.of(
            requestDTO.getPage() != null ? requestDTO.getPage() : 0,
            requestDTO.getSize() != null ? requestDTO.getSize() : 10);

        Page<Challenge> page = challengeRepository.searchOpenChallenges(
            requestDTO.getKeyword(), requestDTO.getSortType(), pageable);

        List<ChallengeListResDTO> content = page.getContent().stream()
            .map(ChallengeAssembler::toChallengeListResDTO)
            .collect(Collectors.toList());

        return PageResponseDTO.<ChallengeListResDTO>builder()
            .content(content)
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .build();
    }

    @Override
    public void likeChallenge(Long challengeId, Long memberId) {
        // 챌린지 존재 확인
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_CHALLENGE));

        // 회원 존재 확인
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        // 이미 찜한 챌린지인지 확인
        if (challengeLikeRepository.existsByMemberIdAndChallengeId(memberId, challengeId)) {
            throw new BaseException(BaseResponseCode.ALREADY_LIKED_CHALLENGE);
        }

        // ChallengeLike 엔티티 생성 및 저장
        ChallengeLike challengeLike = ChallengeLike.builder()
            .member(member)
            .challenge(challenge)
            .build();
        challengeLikeRepository.save(challengeLike);

        // 챌린지 찜 수 증가
        challenge.incrementLikeCount();
    }

    @Override
    public void unlikeChallenge(Long challengeId, Long memberId) {
        // 챌린지 존재 확인
        Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_CHALLENGE));

        // 회원 존재 확인
        memberRepository.findById(memberId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_FOUND_MEMBER));

        // 찜한 기록 조회
        ChallengeLike challengeLike = challengeLikeRepository.findByMemberIdAndChallengeId(memberId,
                challengeId)
            .orElseThrow(() -> new BaseException(BaseResponseCode.NOT_LIKED_CHALLENGE));

        // ChallengeLike 엔티티 삭제
        challengeLikeRepository.delete(challengeLike);

        // 챌린지 찜 수 감소
        challenge.decrementLikeCount();
    }
}
