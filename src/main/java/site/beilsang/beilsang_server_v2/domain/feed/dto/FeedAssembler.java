package site.beilsang.beilsang_server_v2.domain.feed.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedListResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;

public class FeedAssembler {

    public static PreviewFeedResDTO toEntity(Feed feed) {
        return PreviewFeedResDTO.builder()
            .feedId(feed.getId())
            .feedUrl(feed.getFeedUrl())
            .day(ChronoUnit.DAYS.between(feed.getUploadDate(), LocalDate.now()))
            .build();
    }

    public static List<PreviewFeedResDTO> toEntities(List<Feed> feedList) {
        return feedList.stream().map(FeedAssembler::toEntity).toList();
    }
    public static PreviewFeedListResDTO toPreviewFeedListResDTO(List<Feed> feedList, Boolean hasNext) {
        return PreviewFeedListResDTO.builder()
            .feeds(toEntities(feedList))
            .hasNext(hasNext)
            .build();
    }
}
