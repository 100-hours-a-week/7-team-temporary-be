package molip.server.ai.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AiPlannerConfig {

    @Value("${ai.planner.timeout-ms:300000}")
    private long timeoutMs;

    @Bean
    public RestTemplate aiPlannerRestTemplate(RestTemplateBuilder restTemplateBuilder) {

        return restTemplateBuilder
                .connectTimeout(Duration.ofMillis(timeoutMs))
                .readTimeout(Duration.ofMillis(timeoutMs))
                .build();
    }
}
