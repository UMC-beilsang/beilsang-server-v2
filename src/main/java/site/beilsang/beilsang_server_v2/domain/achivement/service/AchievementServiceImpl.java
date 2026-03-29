package site.beilsang.beilsang_server_v2.domain.achivement.service;

import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.ChallengeAssembler;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.HallOfFameListResDto;
import site.beilsang.beilsang_server_v2.domain.challenge.dto.res.HallOfFameResDTO;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.challenge.repository.ChallengeRepository;
import site.beilsang.beilsang_server_v2.domain.feed.dto.FeedAssembler;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.feed.repository.FeedRepository;
import site.beilsang.beilsang_server_v2.global.common.SliceResponseDTO;
import site.beilsang.beilsang_server_v2.global.enums.Category;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AchievementServiceImpl implements AchievementService {

    private final ChallengeRepository challengeRepository;
    private final FeedRepository feedRepository;

    @Override
    public HallOfFameListResDto getCategoryHallOfFame(Category category) {
        List<Challenge> topChallenges;
        if (category == Category.ALL) {
            topChallenges = challengeRepository.findTop10ByOrderByCountLikesDescStartDateDesc();
        } else {
            topChallenges = challengeRepository.findTop10ByCategoryOrderByCountLikesDescStartDateDesc(
                category);
        }

        List<HallOfFameResDTO> hallOfFameItems = IntStream.range(0, topChallenges.size())
            .mapToObj(index -> {
                Challenge challenge = topChallenges.get(index);
                return ChallengeAssembler.toHallOfFameResDTO(challenge, index + 1);
            })
            .toList();

        return HallOfFameListResDto.builder()
            .category(category)
            .challenges(hallOfFameItems)
            .build();
    }

    @Override
    public SliceResponseDTO<PreviewFeedResDTO> getFeedsByCategory(Category category, Integer page,
        Integer size, Long memberId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Slice<Feed> feedsPage;
        if (category == Category.ALL) {
            feedsPage = feedRepository.findAll(pageable);
        } else {
            feedsPage = feedRepository.findAllByChallenge_Category(category, pageable);
        }
        return FeedAssembler.toSliceResponseDTO(feedsPage, memberId);
    }
}
