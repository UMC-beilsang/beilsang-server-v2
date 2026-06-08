package site.beilsang.beilsang_server_v2;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// AWS, Apple OAuth 등 외부 환경변수가 필요한 통합 테스트
// CI/CD 파이프라인에서 실제 환경변수 주입 후 실행
@Disabled("외부 환경변수(AWS, Apple OAuth 등) 없이는 실행 불가 — CI/CD 환경에서만 실행")
@SpringBootTest
class BeilsangServerV2ApplicationTests {

	@Test
	void contextLoads() {
	}

}
