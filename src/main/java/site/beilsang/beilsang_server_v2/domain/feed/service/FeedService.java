package site.beilsang.beilsang_server_v2.domain.feed.service;


import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedListResDTO;
import site.beilsang.beilsang_server_v2.global.enums.Category;

public interface FeedService {
    PreviewFeedListResDTO getFeedsByCategory(Category category, Integer page);
}
