package molip.server.report.facade;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import molip.server.ai.client.AiReportChatStreamClient;
import molip.server.ai.client.AiReportChatStreamClient.AiReportChatStreamEvent;
import molip.server.auth.store.redis.RedisDeviceStore;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;
import molip.server.report.entity.Report;
import molip.server.report.service.ReportChatMessageService;
import molip.server.report.service.ReportService;
import molip.server.socket.dto.response.SocketReportStreamChunkResponse;
import molip.server.socket.dto.response.SocketReportStreamCompleteResponse;
import molip.server.socket.dto.response.SocketReportStreamErrorResponse;
import molip.server.socket.dto.response.SocketReportStreamStartResponse;
import molip.server.socket.service.SocketReportChannelBroadcaster;
import molip.server.socket.store.RedisSocketSessionStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportChatStreamFacade {

    private static final String EVENT_START = "start";
    private static final String EVENT_CHUNK = "chunk";
    private static final String EVENT_COMPLETE = "complete";
    private static final String EVENT_ERROR = "error";

    private static final String SOCKET_EVENT_START = "report.stream.start";
    private static final String SOCKET_EVENT_CHUNK = "report.stream.chunk";
    private static final String SOCKET_EVENT_COMPLETE = "report.stream.complete";
    private static final String SOCKET_EVENT_ERROR = "report.stream.error";

    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_FAILED = "FAILED";
    private static final String STATUS_GENERATING = "GENERATING";

    private static final String ERROR_CODE_INTERNAL = "CHAT_STREAM_INTERNAL_ERROR";
    private static final String INTERNAL_ERROR_MESSAGE = "응답 생성 중 오류가 발생했습니다.";

    private final AiReportChatStreamClient aiReportChatStreamClient;
    private final ReportService reportService;
    private final ReportChatMessageService reportChatMessageService;
    private final RedisDeviceStore deviceStore;
    private final RedisSocketSessionStore socketSessionStore;
    private final SocketReportChannelBroadcaster socketReportChannelBroadcaster;

    @Qualifier("aiReportChatStreamTaskExecutor")
    private final TaskExecutor aiReportChatStreamTaskExecutor;

    private final Map<Long, StreamAccumulator> activeStreams = new ConcurrentHashMap<>();

    public void startStream(Long reportId, Long messageId) {
        if (reportId == null || messageId == null) {
            log.warn(
                    "report stream skipped: reportId or messageId is null. reportId={}, messageId={}",
                    reportId,
                    messageId);
            return;
        }

        Report report = reportService.getReportWithUserId(reportId);
        Long userId = report.getUser().getId();
        log.info(
                "report stream start requested: reportId={}, messageId={}, userId={}",
                reportId,
                messageId,
                userId);

        StreamAccumulator accumulator = new StreamAccumulator();
        StreamAccumulator existing = activeStreams.putIfAbsent(messageId, accumulator);

        if (existing != null) {
            log.info(
                    "report stream already active: reportId={}, messageId={}, userId={}",
                    reportId,
                    messageId,
                    userId);
            return;
        }

        aiReportChatStreamTaskExecutor.execute(
                () -> {
                    try {
                        aiReportChatStreamClient.stream(
                                reportId,
                                messageId,
                                event ->
                                        handleStreamEvent(
                                                reportId, userId, messageId, accumulator, event));

                    } catch (Exception ignored) {
                        log.error(
                                "report stream execution failed: reportId={}, messageId={}, userId={}",
                                reportId,
                                messageId,
                                userId);

                        broadcastError(reportId, userId, messageId);
                        reportChatMessageService.deleteAiStreamMessage(messageId);

                    } finally {

                        activeStreams.remove(messageId);
                        log.info(
                                "report stream finished: reportId={}, messageId={}, userId={}",
                                reportId,
                                messageId,
                                userId);
                    }
                });
    }

    private void handleStreamEvent(
            Long reportId,
            Long userId,
            Long messageId,
            StreamAccumulator accumulator,
            AiReportChatStreamEvent event) {

        switch (event.eventType()) {
            case EVENT_START -> handleStart(reportId, userId, messageId, accumulator, event.data());

            case EVENT_CHUNK -> handleChunk(reportId, userId, messageId, accumulator, event.data());

            case EVENT_COMPLETE ->
                    handleComplete(reportId, userId, messageId, accumulator, event.data());

            case EVENT_ERROR -> handleError(reportId, userId, messageId);

            default -> {}
        }
    }

    private void handleStart(
            Long reportId,
            Long userId,
            Long messageId,
            StreamAccumulator accumulator,
            JsonNode data) {
        accumulator.started = true;
        log.info(
                "report stream event start: reportId={}, messageId={}, userId={}",
                reportId,
                messageId,
                userId);

        broadcastToUser(
                userId,
                SOCKET_EVENT_START,
                SocketReportStreamStartResponse.of(
                        reportId,
                        messageId,
                        resolveSenderType(data),
                        resolveMessageType(data),
                        resolveStatus(data, STATUS_GENERATING)));
    }

    private void handleChunk(
            Long reportId,
            Long userId,
            Long messageId,
            StreamAccumulator accumulator,
            JsonNode data) {
        long sequence = data.path("sequence").asLong(0L);

        if (sequence <= accumulator.lastSequence) {
            log.debug(
                    "report stream chunk skipped by sequence guard: reportId={}, messageId={}, userId={}, sequence={}, lastSequence={}",
                    reportId,
                    messageId,
                    userId,
                    sequence,
                    accumulator.lastSequence);
            return;
        }

        accumulator.lastSequence = sequence;

        String delta = data.path("delta").asText("");

        if (!delta.isEmpty()) {
            accumulator.content.append(delta);
        }
        log.info(
                "report stream event chunk: reportId={}, messageId={}, userId={}, sequence={}, deltaLength={}",
                reportId,
                messageId,
                userId,
                sequence,
                delta.length());

        broadcastToUser(
                userId,
                SOCKET_EVENT_CHUNK,
                SocketReportStreamChunkResponse.of(
                        reportId,
                        messageId,
                        resolveSenderType(data),
                        resolveMessageType(data),
                        delta,
                        sequence));
    }

    private void handleComplete(
            Long reportId,
            Long userId,
            Long messageId,
            StreamAccumulator accumulator,
            JsonNode data) {
        String status = data.path("status").asText();
        log.info(
                "report stream event complete: reportId={}, messageId={}, userId={}, status={}",
                reportId,
                messageId,
                userId,
                status);

        broadcastToUser(
                userId,
                SOCKET_EVENT_COMPLETE,
                SocketReportStreamCompleteResponse.of(
                        reportId,
                        messageId,
                        resolveSenderType(data),
                        resolveMessageType(data),
                        status));

        if (!STATUS_COMPLETED.equals(status)) {
            log.info(
                    "report stream complete without completed status. delete placeholder: reportId={}, messageId={}, userId={}, status={}",
                    reportId,
                    messageId,
                    userId,
                    status);
            reportChatMessageService.deleteAiStreamMessage(messageId);
            return;
        }

        reportChatMessageService.completeAiStreamMessage(messageId, accumulator.content.toString());
        log.info(
                "report stream content persisted: reportId={}, messageId={}, userId={}, contentLength={}",
                reportId,
                messageId,
                userId,
                accumulator.content.length());
    }

    private void handleError(Long reportId, Long userId, Long messageId) {
        log.warn(
                "report stream event error: reportId={}, messageId={}, userId={}",
                reportId,
                messageId,
                userId);
        broadcastError(reportId, userId, messageId);
        reportChatMessageService.deleteAiStreamMessage(messageId);
    }

    private void broadcastError(Long reportId, Long userId, Long messageId) {
        log.info(
                "report stream broadcast error event: reportId={}, messageId={}, userId={}",
                reportId,
                messageId,
                userId);
        broadcastToUser(
                userId,
                SOCKET_EVENT_ERROR,
                SocketReportStreamErrorResponse.of(
                        reportId,
                        messageId,
                        STATUS_FAILED,
                        ERROR_CODE_INTERNAL,
                        INTERNAL_ERROR_MESSAGE));
    }

    private void broadcastToUser(Long userId, String event, Object payload) {
        Set<String> deviceIds = deviceStore.listDevices(userId);
        log.info(
                "report stream broadcast begin: event={}, userId={}, deviceCount={}",
                event,
                userId,
                deviceIds.size());

        for (String deviceId : deviceIds) {
            String sessionId = socketSessionStore.findSessionId(userId, deviceId);

            if (sessionId == null || sessionId.isBlank()) {
                log.debug(
                        "report stream broadcast skip empty session: event={}, userId={}, deviceId={}",
                        event,
                        userId,
                        deviceId);
                continue;
            }

            log.info(
                    "report stream broadcast to session: event={}, userId={}, deviceId={}, sessionId={}",
                    event,
                    userId,
                    deviceId,
                    sessionId);
            socketReportChannelBroadcaster.sendToSession(sessionId, event, payload);
        }
    }

    private SenderType resolveSenderType(JsonNode data) {
        String senderType = data.path("senderType").asText(SenderType.AI.name());

        try {
            return SenderType.valueOf(senderType);
        } catch (IllegalArgumentException exception) {
            return SenderType.AI;
        }
    }

    private MessageType resolveMessageType(JsonNode data) {
        String messageType = data.path("messageType").asText(MessageType.TEXT.name());

        try {
            return MessageType.valueOf(messageType);
        } catch (IllegalArgumentException exception) {
            return MessageType.TEXT;
        }
    }

    private String resolveStatus(JsonNode data, String defaultStatus) {
        String status = data.path("status").asText();

        if (status == null || status.isBlank()) {
            return defaultStatus;
        }

        return status;
    }

    private static final class StreamAccumulator {

        private boolean started;
        private long lastSequence;
        private final StringBuilder content = new StringBuilder();
    }
}
