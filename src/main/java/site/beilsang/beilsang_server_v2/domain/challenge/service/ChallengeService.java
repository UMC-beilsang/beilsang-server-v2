package site.beilsang.beilsang_server_v2.domain.challenge.service;

import java.util.List;
import lombok.extern.java.Log;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListRequestDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeListResponseDTO;
import site.beilsang.beilsang_server_v2.global.common.PageResponseDTO;

public interface ChallengeService {

    ChallengeResDTO createChallenge(Long memberId, CreateChallengeReqDTO createChallengeReqDTO,
            List<MultipartFile> infoImages, List<MultipartFile> certImages);

    PageResponseDTO<ChallengeListResponseDTO> getChallengeList(ChallengeListRequestDTO requestDTO);
}
