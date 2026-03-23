package molip.server.report.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
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
    public boolean saveFirstAiSummaryMessage(Report report, String content) {
        if (report == null || report.getId() == null) {
            throw new BaseException(ErrorCode.REPORT_NOT_FOUND_GENERIC);
        }

        if (content == null || content.isBlank()) {
            return false;
        }

        if (reportChatMessageRepository.existsByReportIdAndDeletedAtIsNullAndIsDeletedFalse(
                report.getId())) {
            return false;
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

        return true;
    }

    @Transactional(readOnly = true)
    public List<ReportChatMessage> getPromptMessages(Long reportId) {
        if (reportId == null) {
            throw new BaseException(ErrorCode.REPORT_NOT_FOUND_GENERIC);
        }

        return reportChatMessageRepository
                .findByReportIdAndDeletedAtIsNullAndIsDeletedFalseAndContentIsNotNullOrderByIdAsc(
                        reportId);
    }

    @Transactional(readOnly = true)
    public void validateStreamOpen(Long reportId, Long messageId) {
        if (reportId == null || messageId == null) {
            throw new BaseException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        ReportChatMessage message =
                reportChatMessageRepository
                        .findByIdAndDeletedAtIsNullAndIsDeletedFalse(messageId)
                        .orElse(null);

        if (message == null) {
            if (reportChatMessageRepository.existsByIdAndReportId(messageId, reportId)) {
                throw new BaseException(ErrorCode.CONFLICT_STREAM_ENDED);
            }

            throw new BaseException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        if (!message.getReport().getId().equals(reportId)
                || message.getSenderType() != SenderType.AI) {
            throw new BaseException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        if (message.getContent() != null) {
            throw new BaseException(ErrorCode.CONFLICT_STREAM_ENDED);
        }
    }

    @Transactional(readOnly = true)
    public ReportChatMessage getStreamMessage(Long reportId, Long streamMessageId) {
        if (reportId == null || streamMessageId == null) {
            throw new BaseException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        ReportChatMessage message =
                reportChatMessageRepository
                        .findByIdAndReportId(streamMessageId, reportId)
                        .orElseThrow(() -> new BaseException(ErrorCode.MESSAGE_NOT_FOUND));

        if (message.getSenderType() != SenderType.AI) {
            throw new BaseException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        return message;
    }

    @Transactional(readOnly = true)
    public Long findInputMessageId(Long reportId, Long streamMessageId) {
        return reportChatMessageRepository
                .findTopByReportIdAndSenderTypeAndIdLessThanOrderByIdDesc(
                        reportId, SenderType.USER, streamMessageId)
                .map(ReportChatMessage::getId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public ReportChatMessage getLatestActiveAiStreamMessage(Long reportId) {
        if (reportId == null) {
            throw new BaseException(ErrorCode.REPORT_NOT_FOUND_GENERIC);
        }

        return reportChatMessageRepository
                .findTopByReportIdAndSenderTypeAndContentIsNullAndDeletedAtIsNullAndIsDeletedFalseOrderByIdDesc(
                        reportId, SenderType.AI)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public ReportChatMessage getLatestUserMessageBefore(Long reportId, Long streamMessageId) {
        if (reportId == null || streamMessageId == null) {
            throw new BaseException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        return reportChatMessageRepository
                .findTopByReportIdAndSenderTypeAndIdLessThanAndDeletedAtIsNullAndIsDeletedFalseOrderByIdDesc(
                        reportId, SenderType.USER, streamMessageId)
                .orElse(null);
    }

    @Transactional
    public void completeAiStreamMessage(Long messageId, String content) {
        ReportChatMessage message = getActiveMessage(messageId);

        message.updateContent(content == null ? "" : content);
    }

    @Transactional
    public void deleteAiStreamMessage(Long messageId) {
        ReportChatMessage message = getActiveMessage(messageId);

        message.deleteMessage();
    }

    @Transactional
    public void deleteAiStreamMessageIfExists(Long messageId) {
        if (messageId == null) {
            return;
        }

        reportChatMessageRepository
                .findByIdAndDeletedAtIsNullAndIsDeletedFalse(messageId)
                .ifPresent(ReportChatMessage::deleteMessage);
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

    private ReportChatMessage getActiveMessage(Long messageId) {
        if (messageId == null) {
            throw new BaseException(ErrorCode.MESSAGE_NOT_FOUND);
        }

        return reportChatMessageRepository
                .findByIdAndDeletedAtIsNullAndIsDeletedFalse(messageId)
                .orElseThrow(() -> new BaseException(ErrorCode.MESSAGE_NOT_FOUND));
    }

    private String normalizeContent(String inputMessage) {
        String normalized = inputMessage.trim();

        return normalized.isEmpty() ? null : normalized;
    }
}
