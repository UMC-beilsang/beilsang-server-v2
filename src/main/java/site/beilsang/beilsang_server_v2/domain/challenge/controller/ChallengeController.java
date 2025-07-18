package site.beilsang.beilsang_server_v2.domain.challenge.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.ChallengeListReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.req.CreateChallengeReqDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeDetailResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeListResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.ChallengeResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.service.ChallengeService;
import site.beilsang.beilsang_server_v2.global.common.BaseResponse;
import site.beilsang.beilsang_server_v2.global.common.PageResponseDTO;

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

    @GetMapping
    public BaseResponse<PageResponseDTO<ChallengeListResDTO>> getChallengeList(
            Authentication authentication,
            @ModelAttribute ChallengeListReqDTO requestDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        requestDTO.setMemberId(memberId);
        return new BaseResponse<>(challengeService.getChallengeList(requestDTO));
    }

    @GetMapping("/{challengeId}")
    public BaseResponse<ChallengeDetailResDTO> getChallengeDetail(
            @PathVariable Long challengeId) {
        // TODO: 서비스 호출 및 상세 정보 반환 구현
        return null;
    }
}
