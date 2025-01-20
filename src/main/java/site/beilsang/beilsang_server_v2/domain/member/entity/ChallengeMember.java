package site.beilsang.beilsang_server_v2.domain.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.beilsang.beilsang_server_v2.domain.challenge.entity.Challenge;
import site.beilsang.beilsang_server_v2.domain.feed.entity.Feed;
import site.beilsang.beilsang_server_v2.global.common.BaseEntity;
import site.beilsang.beilsang_server_v2.global.enums.ChallengeStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_member_id")
    private Long id;

    private Boolean isHost;

    private Integer successDays;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus challengeStatus;

    private Boolean isFeedUpload;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;


    @OneToMany(mappedBy = "challengeMember", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Feed> feed = new ArrayList<>();
}
