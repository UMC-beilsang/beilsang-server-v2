package site.beilsang.beilsang_server_v2.global.enums;

public enum ChallengeReportReason {
    UNRELATED,    // 친환경 챌린지 목적과 무관함
    SPAM,         // 스팸 / 광고 / 홍보성 챌린지
    INAPPROPRIATE, // 욕설·혐오·선정적 등 부적절한 내용 포함
    ILLEGAL,      // 위험하거나 불법적인 행위를 유도함
    ETC           // 기타 (직접 입력)
}
