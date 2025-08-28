package site.beilsang.beilsang_server_v2.domain.point.dto.res;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PointLogListResDTO {

    private Integer total;
    private List<PointLogResDTO> points;
}
