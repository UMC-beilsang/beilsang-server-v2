package site.beilsang.beilsang_server_v2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

// AWS, Apple OAuth 등 외부 환경변수가 모두 주입된 환경(CI/CD)에서만 실행
@EnabledIfEnvironmentVariable(named = "AWS_ACCESS_KEY_ID", matches = ".+")
@SpringBootTest
class BeilsangServerV2ApplicationTests {

	@Test
	void contextLoads() {
	}

}
