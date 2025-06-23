package site.beilsang.beilsang_server_v2.domain.challenge.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;

public interface ChallengeService {

    ChallengeResDTO createChallenge(Long memberId, CreateChallengeReqDTO createChallengeReqDTO,
            List<MultipartFile> infoImages, List<MultipartFile> certImages);
}
