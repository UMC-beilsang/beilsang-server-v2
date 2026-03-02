package site.beilsang.beilsang_server_v2.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Arrays;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class SwaggerCustomizerConfig {

    @Component
    public static class SwaggerServerCustomizer implements OpenApiCustomizer {

        @Override
        public void customise(OpenAPI openApi) {
            Server localServer = new Server();
            localServer.setUrl("http://localhost:8080");
            localServer.setDescription("Local 개발 서버");

            Server stageServer = new Server();
            stageServer.setUrl("https://stage.beilsang.xyz");
            stageServer.setDescription("Stage 서버");

            Server prodServer = new Server();
            prodServer.setUrl("https://prod.beilsang.xyz");
            prodServer.setDescription("Production 서버");

            openApi.setServers(Arrays.asList(localServer, stageServer, prodServer));
        }
    }
}
