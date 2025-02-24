package site.beilsang.beilsang_server_v2.domain.point.dto.res;

import lombok.Builder;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.global.enums.PointStatus;

import java.time.LocalDate;

@Getter
@Builder
public class PointLogResDTO {

    private Long id;
    private String name;
    private PointStatus status;
    private Integer value;
    private LocalDate date;
    private Integer period;
}
