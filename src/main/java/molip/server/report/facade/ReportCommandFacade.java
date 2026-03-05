package molip.server.report.facade;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import molip.server.ai.client.AiReportChatClient;
import molip.server.ai.dto.request.AiReportChatMessageRequest;
import molip.server.ai.dto.request.AiReportChatRespondRequest;
import molip.server.ai.dto.response.AiReportChatRespondResponse;
import molip.server.auth.store.redis.RedisDeviceStore;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.report.dto.response.ReportMessageCreateResponse;
import molip.server.report.entity.Report;
import molip.server.report.entity.ReportChatMessage;
import molip.server.report.event.ReportChatRespondRequestedEvent;
import molip.server.report.event.ReportMessageCreatedEvent;
import molip.server.report.redis.RedisReportChatStreamStore;
import molip.server.report.service.ReportChatMessageService;
import molip.server.report.service.ReportService;
import molip.server.socket.dto.response.SocketReportStreamCompleteResponse;
import molip.server.socket.service.SocketReportChannelBroadcaster;
import molip.server.socket.store.RedisSocketSessionStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReportCommandFacade {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String SOCKET_EVENT_COMPLETE = "report.stream.complete";
    private static final String STATUS_CANCELED = "CANCELED";

    private final AiReportChatClient aiReportChatClient;
    private final ReportService reportService;
    private final ReportChatMessageService reportChatMessageService;
    private final RedisReportChatStreamStore redisReportChatStreamStore;
    private final RedisDeviceStore deviceStore;
    private final RedisSocketSessionStore socketSessionStore;
    private final SocketReportChannelBroadcaster socketReportChannelBroadcaster;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReportMessageCreateResponse createReportMessage(
            Long userId, Long reportId, String inputMessage) {
        Report report = reportService.getReportWithUserId(userId, reportId);

        validateReportAvailability(report);

        reportChatMessageService.validateNoActiveAiResponse(reportId);

        ReportChatMessage inputMessageEntity =
                reportChatMessageService.createUserMessage(report, inputMessage);

        ReportChatMessage streamMessageEntity =
                reportChatMessageService.createAiStreamMessage(report);

        redisReportChatStreamStore.initialize(
                reportId, inputMessageEntity.getId(), streamMessageEntity.getId());

        requestAiRespond(userId, reportId, streamMessageEntity.getId());

        eventPublisher.publishEvent(
                new ReportMessageCreatedEvent(
                        userId,
                        reportId,
                        inputMessageEntity.getId(),
                        inputMessageEntity.getSenderType(),
                        inputMessageEntity.getMessageType(),
                        inputMessageEntity.getContent(),
                        inputMessageEntity.getSentAt()));

        eventPublisher.publishEvent(
                new ReportChatRespondRequestedEvent(reportId, streamMessageEntity.getId()));

        return ReportMessageCreateResponse.of(
                inputMessageEntity.getId(), streamMessageEntity.getId());
    }

    @Transactional(readOnly = true)
    public ReportMessageCreateResponse resolveDuplicateByRunningMessage(
            Long userId, Long reportId, String inputMessage) {
        Report report = reportService.getReportWithUserId(userId, reportId);

        validateReportAvailability(report);

        ReportChatMessage runningStreamMessage =
                reportChatMessageService.getLatestActiveAiStreamMessage(reportId);

        if (runningStreamMessage == null) {
            return null;
        }

        ReportChatMessage latestUserMessage =
                reportChatMessageService.getLatestUserMessageBefore(
                        reportId, runningStreamMessage.getId());

        if (latestUserMessage == null) {
            return null;
        }

        String normalizedInput = normalizeContent(inputMessage);
        String latestContent = normalizeContent(latestUserMessage.getContent());

        if (normalizedInput == null
                || latestContent == null
                || !normalizedInput.equals(latestContent)) {
            return null;
        }

        return ReportMessageCreateResponse.of(
                latestUserMessage.getId(), runningStreamMessage.getId());
    }

    private void validateReportAvailability(Report report) {
        LocalDateTime availableAt = report.getEndDate().plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now(KOREA_ZONE_ID);

        if (now.isBefore(availableAt)) {
            throw new BaseException(ErrorCode.FORBIDDEN_REPORT_NOT_AVAILABLE_YET);
        }
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return null;
        }

        String normalized = content.trim();

        return normalized.isEmpty() ? null : normalized;
    }

    private void requestAiRespond(Long userId, Long reportId, Long streamMessageId) {
        List<AiReportChatMessageRequest> messages =
                reportChatMessageService.getPromptMessages(reportId).stream()
                        .map(
                                message ->
                                        AiReportChatMessageRequest.of(
                                                message.getId(),
                                                message.getSenderType(),
                                                message.getMessageType(),
                                                message.getContent()))
                        .toList();

        AiReportChatRespondResponse response =
                aiReportChatClient.requestRespond(
                        reportId, AiReportChatRespondRequest.of(userId, streamMessageId, messages));

        if (!streamMessageId.equals(response.data().messageId())) {
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public void cancelReportMessage(Long userId, Long reportId, Long streamMessageId) {
        if (userId == null || reportId == null || streamMessageId == null) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_MISSING_REQUIRED);
        }

        Report report = reportService.getReportWithUserId(userId, reportId);

        validateReportAvailability(report);

        ReportChatMessage streamMessage =
                reportChatMessageService.getStreamMessage(reportId, streamMessageId);

        if (streamMessage.getDeletedAt() != null || streamMessage.isDeleted()) {
            throw new BaseException(ErrorCode.CONFLICT_RESPONSE_ALREADY_ENDED);
        }

        if (streamMessage.getContent() != null) {
            throw new BaseException(ErrorCode.CONFLICT_RESPONSE_ALREADY_ENDED);
        }

        aiReportChatClient.requestCancel(reportId, streamMessageId);

        reportChatMessageService.deleteAiStreamMessageIfExists(streamMessageId);
        redisReportChatStreamStore.updateStatus(reportId, streamMessageId, STATUS_CANCELED);

        broadcastCancelComplete(userId, reportId, streamMessageId);
    }

    private void broadcastCancelComplete(Long userId, Long reportId, Long streamMessageId) {
        Set<String> deviceIds = deviceStore.listDevices(userId);

        for (String deviceId : deviceIds) {
            String sessionId = socketSessionStore.findSessionId(userId, deviceId);

            if (sessionId == null || sessionId.isBlank()) {
                continue;
            }

            socketReportChannelBroadcaster.sendToSession(
                    sessionId,
                    SOCKET_EVENT_COMPLETE,
                    SocketReportStreamCompleteResponse.of(
                            reportId,
                            streamMessageId,
                            SenderType.AI,
                            MessageType.TEXT,
                            STATUS_CANCELED));
        }
    }
}
