package site.beilsang.beilsang_server_v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
//@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
@SpringBootApplication
public class BeilsangServerV2Application {

	public static void main(String[] args) {
		SpringApplication.run(BeilsangServerV2Application.class, args);
	}

}
