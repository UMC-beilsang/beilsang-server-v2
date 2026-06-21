package site.beilsang.beilsang_server_v2.domain.badge.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "대표 배지 설정/해제 DTO")
@Getter
@Setter
@NoArgsConstructor
public class RepresentativeBadgeResDTO {
    private Long badgeMemberId;
}
