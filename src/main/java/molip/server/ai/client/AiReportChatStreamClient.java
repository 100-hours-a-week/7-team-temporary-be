package molip.server.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class AiReportChatStreamClient {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String streamPath;

    public AiReportChatStreamClient(
            ObjectMapper objectMapper,
            @Qualifier("aiReportChatStreamRestTemplate") RestTemplate restTemplate,
            @Value("${ai.chatbot.base-url}") String baseUrl,
            @Value("${ai.chatbot.stream-path}") String streamPath) {

        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.streamPath = streamPath;
    }

    public void stream(
            Long reportId, Long streamMessageId, Consumer<AiReportChatStreamEvent> eventConsumer) {
        try {
            log.info(
                    "ai report stream subscribe request: reportId={}, streamMessageId={}",
                    reportId,
                    streamMessageId);

            restTemplate.execute(
                    baseUrl + streamPath,
                    HttpMethod.GET,
                    request -> request.getHeaders().setAccept(List.of(MediaType.TEXT_EVENT_STREAM)),
                    response -> {
                        consumeStream(response, eventConsumer);
                        return null;
                    },
                    Map.of(
                            "reportId", reportId,
                            "messageId", streamMessageId,
                            "streamMessageId", streamMessageId));

            log.info(
                    "ai report stream subscribe completed: reportId={}, streamMessageId={}",
                    reportId,
                    streamMessageId);
        } catch (Exception exception) {
            log.error(
                    "ai report stream subscribe failed: reportId={}, streamMessageId={}, message={}",
                    reportId,
                    streamMessageId,
                    exception.getMessage(),
                    exception);
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void consumeStream(
            ClientHttpResponse response, Consumer<AiReportChatStreamEvent> eventConsumer)
            throws IOException {
        try (BufferedReader reader =
                new BufferedReader(
                        new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
            String eventType = null;
            StringBuilder dataBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    emitEvent(eventType, dataBuilder, eventConsumer);
                    eventType = null;
                    dataBuilder.setLength(0);
                    continue;
                }

                if (line.startsWith("event:")) {
                    eventType = line.substring("event:".length()).trim();
                    continue;
                }

                if (line.startsWith("data:")) {
                    if (!dataBuilder.isEmpty()) {
                        dataBuilder.append('\n');
                    }

                    dataBuilder.append(line.substring("data:".length()).trim());
                }
            }

            emitEvent(eventType, dataBuilder, eventConsumer);
        }
    }

    private void emitEvent(
            String eventType,
            StringBuilder dataBuilder,
            Consumer<AiReportChatStreamEvent> eventConsumer)
            throws IOException {
        if (eventType == null || dataBuilder.isEmpty()) {
            return;
        }

        log.info("ai report stream raw event: eventType={}, data={}", eventType, dataBuilder);

        JsonNode data = objectMapper.readTree(dataBuilder.toString());

        eventConsumer.accept(AiReportChatStreamEvent.of(eventType, data));
    }

    public record AiReportChatStreamEvent(String eventType, JsonNode data) {

        public static AiReportChatStreamEvent of(String eventType, JsonNode data) {
            return new AiReportChatStreamEvent(eventType, data);
        }
    }
}
