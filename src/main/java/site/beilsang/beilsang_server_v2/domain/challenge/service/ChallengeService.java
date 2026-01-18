package site.beilsang.beilsang_server_v2.domain.challenge.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.SearchChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeDetailResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeListResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.JoinChallengeResDTO;
import site.beilsang.beilsang_server_v2.global.common.PageResponseDTO;

/**
 * 챌린지 관련 비즈니스 로직을 처리하는 서비스 인터페이스 챌린지 생성, 조회, 참여 등의 기능을 제공합니다.
 */
public interface ChallengeService {

    /**
     * 새로운 챌린지를 생성합니다.
     *
     * @param memberId              챌린지 생성자의 회원 ID
     * @param createChallengeReqDTO 챌린지 생성 요청 데이터
     * @param infoImages            챌린지 정보 이미지 파일들
     * @param certImages            인증 방법 이미지 파일들
     * @return 생성된 챌린지 정보
     */
    ChallengeResDTO createChallenge(Long memberId, CreateChallengeReqDTO createChallengeReqDTO,
        List<MultipartFile> infoImages, List<MultipartFile> certImages);

    /**
     * 조건에 따른 챌린지 목록을 페이지네이션으로 조회합니다.
     *
     * @param requestDTO 챌린지 목록 조회 필터 조건
     * @return 페이지네이션된 챌린지 목록
     */
    PageResponseDTO<ChallengeListResDTO> getChallengeList(ChallengeListReqDTO requestDTO);

    /**
     * 특정 챌린지의 상세 정보를 조회합니다.
     *
     * @param challengeId 조회할 챌린지 ID
     * @param memberId    조회 요청하는 회원 ID
     * @return 챌린지 상세 정보
     */
    ChallengeDetailResDTO getChallengeDetail(Long challengeId, Long memberId);

    /**
     * 특정 챌린지에 참여합니다.
     *
     * @param challengeId 참여할 챌린지 ID
     * @param memberId    참여하는 회원 ID
     * @return 챌린지 참여 결과
     */
    JoinChallengeResDTO joinChallenge(Long challengeId, Long memberId);

    /**
     * 모집이 마감된 챌린지를 제목으로 검색합니다.
     * 시작일이 오늘 이전인 챌린지를 대상으로 하며, 오늘 날짜에 가까운 순으로 정렬됩니다.
     *
     * @param requestDTO 검색 조건 (키워드, 페이징 정보)
     * @return 페이지네이션된 검색 결과
     */
    PageResponseDTO<ChallengeListResDTO> searchClosedChallenges(SearchChallengeReqDTO requestDTO);

    /**
     * 모집 중인 챌린지를 제목으로 검색합니다.
     * 시작일이 오늘 이후인 챌린지를 대상으로 하며, 마감 임박순 또는 최신순으로 정렬할 수 있습니다.
     *
     * @param requestDTO 검색 조건 (키워드, 정렬 타입, 페이징 정보)
     * @return 페이지네이션된 검색 결과
     */
    PageResponseDTO<ChallengeListResDTO> searchOpenChallenges(SearchChallengeReqDTO requestDTO);
}
