package site.beilsang.beilsang_server_v2.domain.feed.dto;

import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedResDto;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class FeedAssembler {
    public static PreviewFeedResDto toEntity(Feed feed) {
        return PreviewFeedResDto.builder()
                .feedId(feed.getId())
                .feedUrl(feed.getFeedUrl())
                .day(ChronoUnit.DAYS.between(feed.getUploadDate(), LocalDate.now()))
                .build();
    }

    public static List<PreviewFeedResDto> toEntities(List<Feed> feedList) {
        return feedList.stream().map(FeedAssembler::toEntity).toList();
    }
}
