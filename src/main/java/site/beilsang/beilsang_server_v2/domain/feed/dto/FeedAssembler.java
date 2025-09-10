package site.beilsang.beilsang_server_v2.domain.feed.dto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.data.domain.Slice;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedCreateReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedCreateResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedDetailResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.domain.member.dto.res.PreviewMemberInfoDTO;
import site.beilsang.beilsang_server_v2.domain.member.entity.ChallengeMember;
import site.beilsang.beilsang_server_v2.global.common.SliceResponseDTO;

public class FeedAssembler {

    public static Feed toEntity(FeedCreateReqDTO createReqDTO, String feedUrl, Challenge challenge,
        ChallengeMember challengeMember) {
        return Feed.builder()
            .review(createReqDTO.getReview())
            .uploadDate(LocalDate.now())
            .feedUrl(feedUrl)
            .challenge(challenge)
            .challengeMember(challengeMember)
            .build();
    }

    public static PreviewFeedResDTO toPreviewFeedResDTO(Feed feed) {
        return PreviewFeedResDTO.builder()
            .feedId(feed.getId())
            .feedUrl(feed.getFeedUrl())
            .day(ChronoUnit.DAYS.between(feed.getUploadDate(), LocalDate.now()))
            .build();
    }

    public static List<PreviewFeedResDTO> toPreviewFeedResDTOList(List<Feed> feedList) {
        return feedList.stream().map(FeedAssembler::toPreviewFeedResDTO).toList();
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
     * Spring Data Slice를 SliceResponseDTO로 변환
     */
    public static SliceResponseDTO<PreviewFeedResDTO> toSliceResponseDTO(Slice<Feed> feedSlice) {
        List<PreviewFeedResDTO> feedDtoList = toPreviewFeedResDTOList(feedSlice.getContent());
        return SliceResponseDTO.<PreviewFeedResDTO>builder()
            .content(feedDtoList)
            .number(feedSlice.getNumber())
            .size(feedSlice.getSize())
            .numberOfElements(feedSlice.getNumberOfElements())
            .hasNext(feedSlice.hasNext())
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
