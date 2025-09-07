package site.beilsang.beilsang_server_v2.domain.feed.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedCreateReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedListReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.req.FeedUpdateReqDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedCreateResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedDeleteResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedDetailResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedLikeResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.FeedUpdateResDTO;
import site.beilsang.beilsang_server_v2.domain.feed.dto.res.PreviewFeedResDTO;
import site.beilsang.beilsang_server_v2.global.common.PageResponseDTO;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedServiceImpl implements FeedService {


    @Override
    public PageResponseDTO<PreviewFeedResDTO> getFeedList(Long memberId,
        FeedListReqDTO requestDTO) {
        return null;
    }

    @Override
    public FeedDetailResDTO getFeedDetail(Long feedId, Long memberId) {
        return null;
    }

    @Override
    public FeedCreateResDTO createFeed(Long memberId, FeedCreateReqDTO createReqDTO,
        MultipartFile feedImage) {
        return null;
    }

    @Override
    public FeedUpdateResDTO updateFeed(Long feedId, Long memberId, FeedUpdateReqDTO updateReqDTO,
        MultipartFile feedImage) {
        return null;
    }

    @Override
    public FeedDeleteResDTO deleteFeed(Long feedId, Long memberId) {
        return null;
    }

    @Override
    public FeedLikeResDTO addFeedLike(Long feedId, Long memberId) {
        return null;
    }

    @Override
    public FeedLikeResDTO removeFeedLike(Long feedId, Long memberId) {
        return null;
    }

    @Override
    public PageResponseDTO<PreviewFeedResDTO> getMyFeedList(Long memberId, int page, int size) {
        return null;
    }
}
