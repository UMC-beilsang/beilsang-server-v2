package site.beilsang.beilsang_server_v2.domain.badge.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "대표 배지 설정/해제 DTO")
@Builder
@Getter
public class RepresentativeBadgeResDTO {
    private Long badgeMemberId;
}
