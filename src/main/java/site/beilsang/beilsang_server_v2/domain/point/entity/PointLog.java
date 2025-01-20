package site.beilsang.beilsang_server_v2.domain.point.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.global.common.BaseEntity;
import site.beilsang.beilsang_server_v2.global.enums.PointName;
import site.beilsang.beilsang_server_v2.global.enums.PointStatus;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private PointName pointName;

    @Enumerated(EnumType.STRING)
    private PointStatus status;

    private int value;

    private int period;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
}