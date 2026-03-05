package molip.server.ai.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AiChatbotConfig {

    @Value("${ai.chatbot.connect-timeout-ms:10000}")
    private long connectTimeoutMs;

    @Value("${ai.chatbot.respond-timeout-ms:300000}")
    private long respondTimeoutMs;

    @Value("${ai.chatbot.stream-timeout-ms:300000}")
    private long streamTimeoutMs;

    @Bean(name = "aiReportChatRespondRestTemplate")
    public RestTemplate aiReportChatRespondRestTemplate(RestTemplateBuilder restTemplateBuilder) {

        return restTemplateBuilder
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .readTimeout(Duration.ofMillis(respondTimeoutMs))
                .build();
    }

    @Bean(name = "aiReportChatStreamRestTemplate")
    public RestTemplate aiReportChatStreamRestTemplate(RestTemplateBuilder restTemplateBuilder) {

        return restTemplateBuilder
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .readTimeout(Duration.ofMillis(streamTimeoutMs))
                .build();
    }

    @Bean(name = "aiReportChatStreamTaskExecutor")
    public TaskExecutor aiReportChatStreamTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();

        taskExecutor.setCorePoolSize(2);
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setQueueCapacity(100);
        taskExecutor.setThreadNamePrefix("ai-report-chat-stream-");
        taskExecutor.initialize();

        return taskExecutor;
    }
}
