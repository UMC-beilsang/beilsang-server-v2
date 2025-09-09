package site.beilsang.beilsang_server_v2.domain.feed.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedCreateResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedDetailResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedListResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.PreviewMemberInfoDTO;

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

    public static PreviewFeedListResDTO toPreviewFeedListResDTO(List<Feed> feedList,
        Boolean hasNext) {
        return PreviewFeedListResDTO.builder()
            .feeds(toEntities(feedList))
            .hasNext(hasNext)
            .build();
    }

    /**
     * Feed 엔티티를 FeedDetailResDTO로 변환
     */
    public static FeedDetailResDTO toFeedDetailResDTO(Feed feed, boolean isLiked) {
        return FeedDetailResDTO.builder()
            .feedId(feed.getId())
            .memberInfo(toMemberInfoDTO(feed))
            .challengeId(feed.getChallenge().getId())
            .challengeTitle(feed.getChallenge().getTitle())
            .challengeCategory(feed.getChallenge().getCategory().name())
            .review(feed.getReview())
            .feedUrl(feed.getFeedUrl())
            .uploadDate(feed.getUploadDate())
            .likeCount((long) feed.getFeedLikes().size())
            .isLiked(isLiked)
            .createdAt(feed.getCreatedAt())
            .updatedAt(feed.getUpdatedAt())
            .build();
    }

    /**
     * Feed 엔티티를 FeedCreateResDTO로 변환
     */
    public static FeedCreateResDTO toFeedCreateResDTO(Feed feed) {
        return FeedCreateResDTO.builder()
            .feedId(feed.getId())
            .challengeTitle(feed.getChallenge().getTitle())
            .review(feed.getReview())
            .feedUrl(feed.getFeedUrl())
            .uploadDate(feed.getUploadDate())
            .createdAt(feed.getCreatedAt())
            .build();
    }

    /**
     * Feed 엔티티에서 PreviewMemberInfoDTO로 변환
     */
    private static PreviewMemberInfoDTO toMemberInfoDTO(Feed feed) {
        return PreviewMemberInfoDTO.builder()
            .memberId(feed.getChallengeMember().getMember().getId())
            .nickName(feed.getChallengeMember().getMember().getNickName())
            .profileImage(feed.getChallengeMember().getMember().getProfileUrl())
            .build();
    }
}
