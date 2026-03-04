package molip.server.report.service;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import molip.server.ai.client.AiReportChatStreamClient;
import molip.server.ai.client.AiReportChatStreamClient.AiReportChatStreamEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportChatStreamService {

    private static final String EVENT_START = "start";
    private static final String EVENT_CHUNK = "chunk";
    private static final String EVENT_COMPLETE = "complete";
    private static final String EVENT_ERROR = "error";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final AiReportChatStreamClient aiReportChatStreamClient;
    private final ReportChatMessageService reportChatMessageService;

    @Qualifier("aiReportChatStreamTaskExecutor")
    private final TaskExecutor aiReportChatStreamTaskExecutor;

    private final Map<Long, StreamAccumulator> activeStreams = new ConcurrentHashMap<>();

    public void startStream(Long reportId, Long messageId) {
        if (reportId == null || messageId == null) {
            return;
        }

        StreamAccumulator accumulator = new StreamAccumulator();
        StreamAccumulator existing = activeStreams.putIfAbsent(messageId, accumulator);

        if (existing != null) {
            return;
        }

        aiReportChatStreamTaskExecutor.execute(
                () -> {
                    try {
                        aiReportChatStreamClient.stream(
                                reportId,
                                messageId,
                                event -> handleStreamEvent(messageId, accumulator, event));
                    } catch (Exception ignored) {
                        reportChatMessageService.deleteAiStreamMessage(messageId);
                    } finally {
                        activeStreams.remove(messageId);
                    }
                });
    }

    private void handleStreamEvent(
            Long messageId, StreamAccumulator accumulator, AiReportChatStreamEvent event) {
        switch (event.eventType()) {
            case EVENT_START -> handleStart(accumulator);
            case EVENT_CHUNK -> handleChunk(accumulator, event.data());
            case EVENT_COMPLETE -> handleComplete(messageId, accumulator, event.data());
            case EVENT_ERROR -> handleError(messageId);
            default -> {}
        }
    }

    private void handleStart(StreamAccumulator accumulator) {
        accumulator.started = true;
    }

    private void handleChunk(StreamAccumulator accumulator, JsonNode data) {
        long sequence = data.path("sequence").asLong(0L);

        if (sequence <= accumulator.lastSequence) {
            return;
        }

        accumulator.lastSequence = sequence;

        String delta = data.path("delta").asText("");

        if (!delta.isEmpty()) {
            accumulator.content.append(delta);
        }
    }

    private void handleComplete(Long messageId, StreamAccumulator accumulator, JsonNode data) {
        String status = data.path("status").asText();

        if (!STATUS_COMPLETED.equals(status)) {
            reportChatMessageService.deleteAiStreamMessage(messageId);
            return;
        }

        reportChatMessageService.completeAiStreamMessage(messageId, accumulator.content.toString());
    }

    private void handleError(Long messageId) {
        reportChatMessageService.deleteAiStreamMessage(messageId);
    }

    private static final class StreamAccumulator {

        private boolean started;
        private long lastSequence;
        private final StringBuilder content = new StringBuilder();
    }
}
