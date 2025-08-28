package site.beilsang.beilsang_server_v2.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 3 설정 클래스
 * API 문서화를 위한 기본 정보 및 보안 스키마를 정의합니다.
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 스펙 정의
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(getApiInfo())
                .components(new Components()
                        .addSecuritySchemes("bearer-token", getSecurityScheme()))
                .addSecurityItem(new SecurityRequirement().addList("bearer-token"));
    }

    /**
     * API 기본 정보 설정
     * @return API 정보 객체
     */
    private Info getApiInfo() {
        return new Info()
                .title("Beilsang Server API")
                .description("벌상 챌린지 플랫폼 REST API 문서")
                .version("v2.0")
                .contact(new Contact()
                        .name("Beilsang Team")
                        .email("contact@beilsang.site"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * JWT Bearer 토큰 보안 스키마 설정
     * @return 보안 스키마 객체
     */
    private SecurityScheme getSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT 토큰을 입력하세요 (Bearer 접두사 제외)");
    }
}