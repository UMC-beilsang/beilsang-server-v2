package site.beilsang.beilsang_server_v2.domain.achivement.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.beilsang.beilsang_server_v2.domain.achivement.dto.HallOfFameAssembler;
import site.beilsang.beilsang_server_v2.domain.achivement.dto.res.HallOfFameListResDto;
import site.beilsang.beilsang_server_v2.domain.achivement.dto.res.HallOfFameResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeRepository;
import site.beilsang.beilsang_server_v2.global.enums.Category;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HallOfFameServiceImpl implements HallOfFameService {

    private final ChallengeRepository challengeRepository;

    @Override
    public HallOfFameListResDto getCategoryHallOfFame(Category category) {
        List<Challenge> topChallenges;
        if (category == Category.ALL) {
            topChallenges = challengeRepository.findTop10ByOrderByCountLikesDescStartDateDesc();
        }else{
            topChallenges = challengeRepository.findTop10ByCategoryOrderByCountLikesDescStartDateDesc(category);
        }

        List<HallOfFameResDTO> hallOfFameItems = IntStream.range(0, topChallenges.size())
            .mapToObj(index -> {
                Challenge challenge = topChallenges.get(index);
                return HallOfFameAssembler.toHallfOfFameResDTO(challenge, index + 1);
            })
            .toList();

        return HallOfFameListResDto.builder()
            .category(category)
            .challenges(hallOfFameItems)
            .build();
    }
}
