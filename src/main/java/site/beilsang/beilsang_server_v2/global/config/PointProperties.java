package site.beilsang.beilsang_server_v2.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class PointProperties {

    @Value("${point.reward.new-member}")
    private int newMemberReward;

}
