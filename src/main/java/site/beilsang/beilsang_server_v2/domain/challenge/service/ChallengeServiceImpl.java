package site.beilsang.beilsang_server_v2.domain.challenge.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.CreateChallengeResDTO;

@Service
public class ChallengeServiceImpl implements ChallengeService {

    @Override
    public CreateChallengeResDTO createChallenge(Long memberId, CreateChallengeReqDTO createChallengeReqDTO, MultipartFile mainImage, MultipartFile certImage) {
        return null;
    }
}
