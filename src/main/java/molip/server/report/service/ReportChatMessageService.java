package molip.server.report.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.report.entity.Report;
import molip.server.report.entity.ReportChatMessage;
import molip.server.report.repository.ReportChatMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportChatMessageService {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ReportChatMessageRepository reportChatMessageRepository;

    @Transactional(readOnly = true)
    public Page<ReportChatMessage> getMessages(Long reportId, Long cursor, int size) {
        validateGetMessages(reportId, cursor, size);

        if (cursor == null) {
            return reportChatMessageRepository
                    .findByReportIdAndDeletedAtIsNullAndIsDeletedFalseOrderByIdDesc(
                            reportId, PageRequest.of(0, size));
        }

        return reportChatMessageRepository
                .findByReportIdAndDeletedAtIsNullAndIsDeletedFalseAndIdLessThanOrderByIdDesc(
                        reportId, cursor, PageRequest.of(0, size));
    }

    private void validateGetMessages(Long reportId, Long cursor, int size) {
        if (reportId == null || size <= 0) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_PAGE);
        }

        if (cursor == null) {
            return;
        }

        if (!reportChatMessageRepository.existsByIdAndReportIdAndDeletedAtIsNullAndIsDeletedFalse(
                cursor, reportId)) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INVALID_PAGE);
        }
    }

    @Transactional
    public void saveFirstAiSummaryMessage(Report report, String content) {
        if (report == null || report.getId() == null) {
            throw new BaseException(ErrorCode.REPORT_NOT_FOUND_GENERIC);
        }

        if (content == null || content.isBlank()) {
            return;
        }

        if (reportChatMessageRepository.existsByReportIdAndDeletedAtIsNullAndIsDeletedFalse(
                report.getId())) {
            return;
        }

        ReportChatMessage message =
                new ReportChatMessage(
                        report,
                        SenderType.AI,
                        MessageType.TEXT,
                        content,
                        false,
                        LocalDateTime.now(ZONE_ID));
        reportChatMessageRepository.save(message);
    }
}
