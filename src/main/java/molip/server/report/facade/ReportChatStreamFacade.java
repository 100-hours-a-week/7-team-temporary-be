package molip.server.report.facade;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.report.dto.response.ReportMessageStreamResumeResponse;
import molip.server.report.entity.Report;
import molip.server.report.entity.ReportChatMessage;
import molip.server.report.redis.RedisReportChatStreamStore;
import molip.server.report.redis.ReportChatStreamState;
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
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final AiReportChatStreamClient aiReportChatStreamClient;
    private final ReportService reportService;
    private final ReportChatMessageService reportChatMessageService;
    private final RedisReportChatStreamStore redisReportChatStreamStore;
    private final RedisDeviceStore deviceStore;
    private final RedisSocketSessionStore socketSessionStore;
    private final SocketReportChannelBroadcaster socketReportChannelBroadcaster;

    @Qualifier("aiReportChatStreamTaskExecutor")
    private final TaskExecutor aiReportChatStreamTaskExecutor;

    private final Map<Long, StreamAccumulator> activeStreams = new ConcurrentHashMap<>();

    public ReportMessageStreamResumeResponse resumeStream(
            Long userId, Long reportId, Long streamMessageId) {
        Report report = reportService.getReportWithUserId(userId, reportId);

        validateReportAvailability(report);

        ReportChatMessage streamMessage =
                reportChatMessageService.getStreamMessage(reportId, streamMessageId);

        if (streamMessage.getDeletedAt() != null || streamMessage.isDeleted()) {
            throw new BaseException(ErrorCode.CONFLICT_STREAM_ENDED);
        }

        Long inputMessageId =
                reportChatMessageService.findInputMessageId(reportId, streamMessageId);

        ReportChatStreamState state =
                redisReportChatStreamStore
                        .find(reportId, streamMessageId)
                        .orElseGet(
                                () -> {
                                    redisReportChatStreamStore.initialize(
                                            reportId, inputMessageId, streamMessageId);
                                    return redisReportChatStreamStore
                                            .find(reportId, streamMessageId)
                                            .orElse(
                                                    ReportChatStreamState.initial(
                                                            inputMessageId, streamMessageId));
                                });

        String status = state.status() == null ? STATUS_GENERATING : state.status();
        String content = state.content() == null ? "" : state.content();

        if (STATUS_GENERATING.equals(status)) {
            startStream(reportId, streamMessageId);
        } else if (streamMessage.getContent() != null && !streamMessage.getContent().isBlank()) {
            content = streamMessage.getContent();
            status = STATUS_COMPLETED;
        }
        log.debug(
                "report stream resume requested: reportId={}, streamMessageId={}",
                reportId,
                streamMessageId);

        return ReportMessageStreamResumeResponse.of(
                reportId, inputMessageId, streamMessageId, status, content);
    }

    public void startStream(Long reportId, Long streamMessageId) {
        if (reportId == null || streamMessageId == null) {
            log.warn(
                    "report stream skipped: reportId or streamMessageId is null. reportId={}, streamMessageId={}",
                    reportId,
                    streamMessageId);
            return;
        }

        Report report = reportService.getReportWithUserId(reportId);
        Long userId = report.getUser().getId();

        log.info(
                "report stream start requested: reportId={}, streamMessageId={}, userId={}",
                reportId,
                streamMessageId,
                userId);

        StreamAccumulator accumulator = new StreamAccumulator();
        StreamAccumulator existing = activeStreams.putIfAbsent(streamMessageId, accumulator);

        if (existing != null) {
            log.info(
                    "report stream already active: reportId={}, streamMessageId={}, userId={}",
                    reportId,
                    streamMessageId,
                    userId);
            return;
        }

        redisReportChatStreamStore.updateStatus(reportId, streamMessageId, STATUS_GENERATING);

        aiReportChatStreamTaskExecutor.execute(
                () -> {
                    try {
                        aiReportChatStreamClient.stream(
                                reportId,
                                streamMessageId,
                                event ->
                                        handleStreamEvent(
                                                reportId,
                                                userId,
                                                streamMessageId,
                                                accumulator,
                                                event));

                    } catch (Exception exception) {
                        log.error(
                                "report stream execution failed: reportId={}, streamMessageId={}, userId={}, message={}",
                                reportId,
                                streamMessageId,
                                userId,
                                exception.getMessage(),
                                exception);

                        broadcastError(reportId, userId, streamMessageId);
                        redisReportChatStreamStore.updateStatus(
                                reportId, streamMessageId, STATUS_FAILED);
                        reportChatMessageService.deleteAiStreamMessageIfExists(streamMessageId);

                    } finally {
                        activeStreams.remove(streamMessageId);
                        log.info(
                                "report stream finished: reportId={}, streamMessageId={}, userId={}",
                                reportId,
                                streamMessageId,
                                userId);
                    }
                });
    }

    private void handleStreamEvent(
            Long reportId,
            Long userId,
            Long streamMessageId,
            StreamAccumulator accumulator,
            AiReportChatStreamEvent event) {

        switch (event.eventType()) {
            case EVENT_START ->
                    handleStart(reportId, userId, streamMessageId, accumulator, event.data());

            case EVENT_CHUNK ->
                    handleChunk(reportId, userId, streamMessageId, accumulator, event.data());

            case EVENT_COMPLETE ->
                    handleComplete(reportId, userId, streamMessageId, accumulator, event.data());

            case EVENT_ERROR -> handleError(reportId, userId, streamMessageId, event.data());

            default -> {}
        }
    }

    private void handleStart(
            Long reportId,
            Long userId,
            Long streamMessageId,
            StreamAccumulator accumulator,
            JsonNode data) {
        accumulator.started = true;

        log.info(
                "report stream event start: reportId={}, streamMessageId={}, userId={}",
                reportId,
                streamMessageId,
                userId);

        broadcastToUser(
                userId,
                SOCKET_EVENT_START,
                SocketReportStreamStartResponse.of(
                        reportId,
                        streamMessageId,
                        resolveSenderType(data),
                        resolveMessageType(data),
                        resolveStatus(data, STATUS_GENERATING)));
    }

    private void handleChunk(
            Long reportId,
            Long userId,
            Long streamMessageId,
            StreamAccumulator accumulator,
            JsonNode data) {
        long sequence = data.path("sequence").asLong(0L);

        if (sequence <= accumulator.lastSequence) {
            log.debug(
                    "report stream chunk skipped by sequence guard: reportId={}, streamMessageId={}, userId={}, sequence={}, lastSequence={}",
                    reportId,
                    streamMessageId,
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

        redisReportChatStreamStore.appendChunk(reportId, streamMessageId, sequence, delta);

        log.info(
                "report stream event chunk: reportId={}, streamMessageId={}, userId={}, sequence={}, deltaLength={}",
                reportId,
                streamMessageId,
                userId,
                sequence,
                delta.length());

        broadcastToUser(
                userId,
                SOCKET_EVENT_CHUNK,
                SocketReportStreamChunkResponse.of(
                        reportId,
                        streamMessageId,
                        resolveSenderType(data),
                        resolveMessageType(data),
                        delta,
                        sequence));
    }

    private void handleComplete(
            Long reportId,
            Long userId,
            Long streamMessageId,
            StreamAccumulator accumulator,
            JsonNode data) {
        String status = data.path("status").asText();

        log.info(
                "report stream event complete: reportId={}, streamMessageId={}, userId={}, status={}",
                reportId,
                streamMessageId,
                userId,
                status);

        broadcastToUser(
                userId,
                SOCKET_EVENT_COMPLETE,
                SocketReportStreamCompleteResponse.of(
                        reportId,
                        streamMessageId,
                        resolveSenderType(data),
                        resolveMessageType(data),
                        status));

        if (!STATUS_COMPLETED.equals(status)) {
            redisReportChatStreamStore.updateStatus(reportId, streamMessageId, status);

            log.info(
                    "report stream complete without completed status. delete placeholder: reportId={}, streamMessageId={}, userId={}, status={}",
                    reportId,
                    streamMessageId,
                    userId,
                    status);

            reportChatMessageService.deleteAiStreamMessageIfExists(streamMessageId);
            return;
        }

        reportChatMessageService.completeAiStreamMessage(
                streamMessageId, accumulator.content.toString());
        redisReportChatStreamStore.updateStatus(reportId, streamMessageId, STATUS_COMPLETED);

        log.info(
                "report stream content persisted: reportId={}, streamMessageId={}, userId={}, contentLength={}",
                reportId,
                streamMessageId,
                userId,
                accumulator.content.length());
    }

    private void handleError(Long reportId, Long userId, Long streamMessageId, JsonNode data) {
        log.warn(
                "report stream event error: reportId={}, streamMessageId={}, userId={}, payload={}",
                reportId,
                streamMessageId,
                userId,
                data);

        broadcastError(reportId, userId, streamMessageId);
        redisReportChatStreamStore.updateStatus(reportId, streamMessageId, STATUS_FAILED);
        reportChatMessageService.deleteAiStreamMessageIfExists(streamMessageId);
    }

    private void broadcastError(Long reportId, Long userId, Long streamMessageId) {
        log.info(
                "report stream broadcast error event: reportId={}, streamMessageId={}, userId={}",
                reportId,
                streamMessageId,
                userId);

        broadcastToUser(
                userId,
                SOCKET_EVENT_ERROR,
                SocketReportStreamErrorResponse.of(
                        reportId,
                        streamMessageId,
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

    private void validateReportAvailability(Report report) {
        LocalDateTime availableAt = report.getEndDate().plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now(KOREA_ZONE_ID);

        if (now.isBefore(availableAt)) {
            throw new BaseException(ErrorCode.FORBIDDEN_REPORT_NOT_AVAILABLE_YET);
        }
    }

    private static final class StreamAccumulator {

        private boolean started;
        private long lastSequence;
        private final StringBuilder content = new StringBuilder();
    }
}
