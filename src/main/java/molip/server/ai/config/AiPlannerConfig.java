package molip.server.ai.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AiPlannerConfig {

    @Value("${ai.planner.timeout-ms:300000}")
    private long timeoutMs;

    @Bean(name = "aiPlannerRestTemplate")
    public RestTemplate aiPlannerRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        HttpClient httpClient =
                HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1)
                        .connectTimeout(Duration.ofMillis(timeoutMs))
                        .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofMillis(timeoutMs));

        return restTemplateBuilder.requestFactory(() -> requestFactory).build();
    }
}
