package site.beilsang.beilsang_server_v2.domain.feed.dto.res;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PreviewFeedResDto {
    private Long feedId;
    private String feedUrl;
    private Long day;
}
