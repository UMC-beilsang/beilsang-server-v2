package site.beilsang.beilsang_server_v2.global.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ChallengePeriod {
    WEEK(7), MONTH(30);

    private final Integer days;
}


