package molip.server.report.service;

import java.time.LocalDateTime;
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
    public ReportChatMessage createUserMessage(Report report, String inputMessage) {
        validateCreateUserMessage(report, inputMessage);

        ReportChatMessage message =
                new ReportChatMessage(
                        report,
                        SenderType.USER,
                        MessageType.TEXT,
                        normalizeContent(inputMessage),
                        false,
                        LocalDateTime.now());

        return reportChatMessageRepository.save(message);
    }

    @Transactional
    public ReportChatMessage createAiStreamMessage(Report report) {
        validateCreateAiStreamMessage(report);

        ReportChatMessage message =
                new ReportChatMessage(
                        report, SenderType.AI, MessageType.TEXT, null, false, LocalDateTime.now());

        return reportChatMessageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public void validateNoActiveAiResponse(Long reportId) {
        if (reportId == null) {
            throw new BaseException(ErrorCode.REPORT_NOT_FOUND_GENERIC);
        }

        if (reportChatMessageRepository
                .existsByReportIdAndSenderTypeAndContentIsNullAndDeletedAtIsNullAndIsDeletedFalse(
                        reportId, SenderType.AI)) {
            throw new BaseException(ErrorCode.CONFLICT_REPORT_RESPONSE_RUNNING);
        }
    }

    private void validateCreateUserMessage(Report report, String inputMessage) {
        if (report == null) {
            throw new BaseException(ErrorCode.REPORT_NOT_FOUND_GENERIC);
        }

        if (inputMessage == null || inputMessage.isBlank()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST_INPUT_MESSAGE_REQUIRED);
        }
    }

    private void validateCreateAiStreamMessage(Report report) {
        if (report == null) {
            throw new BaseException(ErrorCode.REPORT_NOT_FOUND_GENERIC);
        }
    }

    private String normalizeContent(String inputMessage) {
        String normalized = inputMessage.trim();

        return normalized.isEmpty() ? null : normalized;
    }
}
