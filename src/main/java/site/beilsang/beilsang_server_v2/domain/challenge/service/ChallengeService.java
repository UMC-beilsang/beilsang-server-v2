package site.beilsang.beilsang_server_v2.domain.challenge.service;

import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.CreateChallengeResDTO;

public interface ChallengeService {

    CreateChallengeResDTO createChallenge(Long memberId, CreateChallengeReqDTO createChallengeReqDTO, MultipartFile mainImage, MultipartFile certImage);
}
