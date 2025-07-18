package site.beilsang.beilsang_server_v2.domain.challenge.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeListResDTO;
import site.beilsang.beilsang_server_v2.global.common.PageResponseDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeDetailResDTO;

public interface ChallengeService {

    ChallengeResDTO createChallenge(Long memberId, CreateChallengeReqDTO createChallengeReqDTO,
            List<MultipartFile> infoImages, List<MultipartFile> certImages);

    PageResponseDTO<ChallengeListResDTO> getChallengeList(ChallengeListReqDTO requestDTO);

    ChallengeDetailResDTO getChallengeDetail(Long challengeId, Long memberId);
}
