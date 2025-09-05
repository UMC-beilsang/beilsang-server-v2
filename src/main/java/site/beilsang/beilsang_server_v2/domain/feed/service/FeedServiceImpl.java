package site.beilsang.beilsang_server_v2.domain.feed.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import site.beilsang.beilsang_server_v2.domain.feed.dto.FeedAssembler;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedListResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.feed.repository.FeedRepository;
import site.beilsang.beilsang_server_v2.global.enums.Category;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {
    private final FeedRepository feedRepository;
    private final Integer PAGE_SIZE = 4;

    public PreviewFeedListResDTO getFeedsByCategory(Category category, Integer page) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());

        Slice<Feed> feedsPage;
        if (category == Category.ALL) {
            feedsPage = feedRepository.findAll(pageable);
        }else{
            feedsPage = feedRepository.findAllByChallenge_Category(category, pageable);
        }
        return FeedAssembler.toPreviewFeedListResDTO(feedsPage.getContent(), feedsPage.hasNext());
    }
}
