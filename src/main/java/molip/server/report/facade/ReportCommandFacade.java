package molip.server.report.facade;

import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.report.dto.response.ReportMessageCreateResponse;
import molip.server.report.entity.Report;
import molip.server.report.entity.ReportChatMessage;
import molip.server.report.service.ReportChatMessageService;
import molip.server.report.service.ReportService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReportCommandFacade {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ReportService reportService;
    private final ReportChatMessageService reportChatMessageService;

    public ReportMessageCreateResponse createReportMessage(
            Long userId, Long reportId, String inputMessage) {
        Report report = reportService.getReport(userId, reportId);

        validateReportAvailability(report);

        reportChatMessageService.validateNoActiveAiResponse(reportId);

        ReportChatMessage inputMessageEntity =
                reportChatMessageService.createUserMessage(report, inputMessage);

        ReportChatMessage streamMessageEntity =
                reportChatMessageService.createAiStreamMessage(report);

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
}
