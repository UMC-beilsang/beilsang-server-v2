package site.beilsang.beilsang_server_v2.domain.challenge.dto.res;

import lombok.Builder;
import lombok.Getter;
import site.beilsang.beilsang_server_v2.global.enums.Category;
import java.util.List;

@Getter
@Builder
public class HallOfFameListResDto {

    private Category category;
    private List<HallOfFameResDTO> challenges;
}
