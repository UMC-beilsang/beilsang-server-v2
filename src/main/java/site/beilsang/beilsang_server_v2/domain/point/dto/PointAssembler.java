package site.beilsang.beilsang_server_v2.domain.point.dto;

import site.beilsang.beilsang_server_v2.domain.member.entity.Member;
import site.beilsang.beilsang_server_v2.domain.point.dto.res.PointLogListResDTO;
import site.beilsang.beilsang_server_v2.domain.point.dto.res.PointLogResDTO;
import site.beilsang.beilsang_server_v2.domain.point.entity.PointLog;

import java.util.List;

public class PointAssembler {

    public static PointLogListResDTO toEntities(List<PointLog> pointLogList, Member member) {
        List<PointLogResDTO> pointLogResDTOList = pointLogList.stream().map(
                PointAssembler::toEntity
        ).toList();
        return PointLogListResDTO.builder()
                .total(member.getPoint())
                .points(pointLogResDTOList)
                .build();
    }

    public static PointLogResDTO toEntity(PointLog pointLog) {
        return PointLogResDTO.builder()
                .id(pointLog.getId())
                .value(pointLog.getValue())
//                .name(pointLog.getName())
//                .period(pointLog.getPeriod())
                .date(pointLog.getCreatedAt().toLocalDate())
                .status(pointLog.getStatus())
                .build();
    }
}
