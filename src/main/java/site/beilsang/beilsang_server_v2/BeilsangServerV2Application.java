package site.beilsang.beilsang_server_v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@EnableScheduling
@EnableFeignClients
@ConfigurationPropertiesScan
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class BeilsangServerV2Application {

    public static void main(String[] args) {
        SpringApplication.run(BeilsangServerV2Application.class, args);
    }

}
