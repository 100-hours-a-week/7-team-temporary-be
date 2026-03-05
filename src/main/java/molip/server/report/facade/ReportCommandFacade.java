package molip.server.report.facade;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import molip.server.ai.client.AiReportChatClient;
import molip.server.ai.dto.request.AiReportChatMessageRequest;
import molip.server.ai.dto.request.AiReportChatRespondRequest;
import molip.server.ai.dto.response.AiReportChatRespondResponse;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.report.dto.response.ReportMessageCreateResponse;
import molip.server.report.entity.Report;
import molip.server.report.entity.ReportChatMessage;
import molip.server.report.event.ReportChatRespondRequestedEvent;
import molip.server.report.service.ReportChatMessageService;
import molip.server.report.service.ReportService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReportCommandFacade {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final AiReportChatClient aiReportChatClient;
    private final ReportService reportService;
    private final ReportChatMessageService reportChatMessageService;
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

        requestAiRespond(userId, reportId, streamMessageEntity.getId());

        eventPublisher.publishEvent(
                new ReportChatRespondRequestedEvent(reportId, streamMessageEntity.getId()));

        return ReportMessageCreateResponse.of(
                inputMessageEntity.getId(), streamMessageEntity.getId());
    }

    private void validateReportAvailability(Report report) {
        LocalDateTime availableAt = report.getEndDate().plusDays(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now(KOREA_ZONE_ID);

        if (now.isBefore(availableAt)) {
            throw new BaseException(ErrorCode.FORBIDDEN_REPORT_NOT_AVAILABLE_YET);
        }
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
}
