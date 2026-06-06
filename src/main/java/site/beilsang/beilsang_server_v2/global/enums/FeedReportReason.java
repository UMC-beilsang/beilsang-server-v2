package site.beilsang.beilsang_server_v2.global.enums;

public enum FeedReportReason {
    UNRELATED,    // 친환경 챌린지 목적과 무관함
    FALSE_CERT,   // 허위 또는 조작(AI)된 이미지
    SPAM,         // 스팸 / 광고 / 홍보성 게시물
    INAPPROPRIATE, // 선정적·폭력적이거나 불쾌감을 주는 이미지
    ETC           // 기타 (직접 입력)
}
