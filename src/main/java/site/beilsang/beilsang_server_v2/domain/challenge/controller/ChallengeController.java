package site.beilsang.beilsang_server_v2.domain.challenge.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.service.ChallengeService;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;

@RestController
@RequestMapping("/challenge")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @PostMapping
    public BaseResponse<ChallengeResDTO> createChallenge(
            Authentication authentication,
            @RequestPart("data") CreateChallengeReqDTO createChallengeReqDTO,
            @RequestPart("infoImages") List<MultipartFile> infoImages,
            @RequestPart("certImages") List<MultipartFile> certImages) {
        Long memberId = (Long) authentication.getPrincipal();
        return new BaseResponse<>(
                challengeService.createChallenge(memberId, createChallengeReqDTO, infoImages, certImages));
    }
}
