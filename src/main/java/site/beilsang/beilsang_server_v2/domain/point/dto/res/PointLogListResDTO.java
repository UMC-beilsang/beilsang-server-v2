package site.beilsang.beilsang_server_v2.domain.point.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PointLogListResDTO {
    private Integer total;
    private List<PointLogResDTO> points;
}
