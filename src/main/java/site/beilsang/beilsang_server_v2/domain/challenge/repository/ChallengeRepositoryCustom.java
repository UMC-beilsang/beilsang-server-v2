package site.beilsang.beilsang_server_v2.domain.challenge.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ClosedChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.OpenChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.global.enums.SearchSortType;

public interface ChallengeRepositoryCustom {

    Page<Challenge> findChallenges(ChallengeListReqDTO requestDTO, Pageable pageable);

    /**
     * 모집중인 챌린지 목록 조회
     * - 시작일이 오늘 이후인 챌린지를 대상으로 조회
     * - 카테고리 필터링 및 정렬 기능 제공
     *
     * @param requestDTO 조회 조건 (카테고리, 정렬 타입)
     * @param pageable   페이징 정보
     * @return 모집중인 챌린지 목록
     */
    Page<Challenge> findOpenChallenges(OpenChallengeListReqDTO requestDTO, Pageable pageable);

    /**
     * 모집마감된 챌린지 목록 조회
     * - 시작일이 오늘 이전인 챌린지를 대상으로 조회
     * - 최근 마감순(startDate DESC)으로 정렬
     *
     * @param requestDTO 조회 조건 (카테고리)
     * @param pageable   페이징 정보
     * @return 모집마감된 챌린지 목록
     */
    Page<Challenge> findClosedChallenges(ClosedChallengeListReqDTO requestDTO, Pageable pageable);

    /**
     * 모집 마감 챌린지 검색
     * - 시작일이 오늘 이전인 챌린지를 검색
     * - 오늘 날짜에 가까운 순으로 정렬 (startDate 내림차순)
     *
     * @param keyword  검색 키워드 (챌린지 제목)
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Challenge> searchClosedChallenges(String keyword, Pageable pageable);

    /**
     * 모집 중인 챌린지 검색
     * - 시작일이 오늘 이후인 챌린지를 검색
     * - 정렬: 마감 임박순(DEADLINE_SOON) 또는 최신순(NEWEST)
     *
     * @param keyword  검색 키워드 (챌린지 제목)
     * @param sortType 정렬 타입
     * @param pageable 페이징 정보
     * @return 검색 결과
     */
    Page<Challenge> searchOpenChallenges(String keyword, SearchSortType sortType, Pageable pageable);
}
