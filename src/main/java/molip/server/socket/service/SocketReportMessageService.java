package molip.server.socket.service;

import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.report.dto.response.ReportMessageCreateResponse;
import molip.server.report.facade.ReportCommandFacade;
import molip.server.socket.dto.request.SocketReportMessageCancelRequest;
import molip.server.socket.dto.request.SocketReportMessageSendRequest;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.dto.response.SocketReportMessageCancelAcceptedResponse;
import molip.server.socket.dto.response.SocketReportMessageCancelRejectedResponse;
import molip.server.socket.dto.response.SocketReportMessageDuplicateResponse;
import molip.server.socket.dto.response.SocketReportMessageSendAcceptedResponse;
import molip.server.socket.dto.response.SocketReportMessageSendRejectedResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocketReportMessageService {

    private static final String EVENT_ACCEPTED = "report.message.sendAccepted";
    private static final String EVENT_REJECTED = "report.message.sendRejected";
    private static final String EVENT_FAILED = "report.message.sendFailed";
    private static final String EVENT_DUPLICATE = "report.message.duplicate";
    private static final String EVENT_CANCEL_ACCEPTED = "report.message.cancelAccepted";
    private static final String EVENT_CANCEL_REJECTED = "report.message.cancelRejected";
    private static final String EVENT_CANCEL_FAILED = "report.message.cancelFailed";

    private final ReportCommandFacade reportCommandFacade;

    public SocketEventResponse<?> sendMessage(Long userId, SocketReportMessageSendRequest request) {
        if (userId == null || request == null || request.reportId() == null) {
            return rejected(
                    request == null ? null : request.reportId(),
                    "REPORT_MESSAGE_INVALID_PAYLOAD",
                    ErrorCode.INVALID_REQUEST_REQUIRED_VALUES.getMessage());
        }

        try {

            ReportMessageCreateResponse response =
                    reportCommandFacade.createReportMessage(
                            userId, request.reportId(), request.inputMessage());

            return SocketEventResponse.of(
                    EVENT_ACCEPTED,
                    SocketReportMessageSendAcceptedResponse.of(
                            request.reportId(),
                            response.inputMessageId(),
                            response.streamMessageId(),
                            "SUCCEEDED"));

        } catch (BaseException exception) {

            return handleBusinessError(
                    userId, request.reportId(), request.inputMessage(), exception.getErrorCode());

        } catch (Exception exception) {

            return failed(
                    request.reportId(),
                    "REPORT_MESSAGE_INTERNAL_ERROR",
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    public SocketEventResponse<?> cancelMessage(
            Long userId, SocketReportMessageCancelRequest request) {
        if (userId == null
                || request == null
                || request.reportId() == null
                || request.messageId() == null) {
            return cancelRejected(
                    request == null ? null : request.reportId(),
                    request == null ? null : request.messageId(),
                    "REPORT_MESSAGE_CANCEL_INVALID_PAYLOAD",
                    ErrorCode.INVALID_REQUEST_REQUIRED_VALUES.getMessage());
        }

        try {
            reportCommandFacade.cancelReportMessage(
                    userId, request.reportId(), request.messageId());

            return SocketEventResponse.of(
                    EVENT_CANCEL_ACCEPTED,
                    SocketReportMessageCancelAcceptedResponse.of(
                            request.reportId(), request.messageId(), "CANCELED"));

        } catch (BaseException exception) {

            return handleCancelBusinessError(
                    request.reportId(), request.messageId(), exception.getErrorCode());

        } catch (Exception exception) {

            return cancelFailed(
                    request.reportId(),
                    request.messageId(),
                    "REPORT_MESSAGE_CANCEL_INTERNAL_ERROR",
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    private SocketEventResponse<?> handleBusinessError(
            Long userId, Long reportId, String inputMessage, ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_REQUEST_INPUT_MESSAGE_REQUIRED ->
                    rejected(reportId, "REPORT_MESSAGE_INVALID_PAYLOAD", errorCode.getMessage());

            case FORBIDDEN_REPORT_ACCESS, FORBIDDEN_REPORT_NOT_AVAILABLE_YET ->
                    rejected(reportId, "REPORT_MESSAGE_FORBIDDEN", errorCode.getMessage());

            case REPORT_NOT_FOUND_GENERIC ->
                    rejected(reportId, "REPORT_MESSAGE_NOT_FOUND", errorCode.getMessage());

            case CONFLICT_REPORT_RESPONSE_RUNNING ->
                    resolveDuplicate(userId, reportId, inputMessage, errorCode);

            default -> failed(reportId, "REPORT_MESSAGE_INTERNAL_ERROR", errorCode.getMessage());
        };
    }

    private SocketEventResponse<?> resolveDuplicate(
            Long userId, Long reportId, String inputMessage, ErrorCode errorCode) {
        ReportMessageCreateResponse duplicate =
                reportCommandFacade.resolveDuplicateByRunningMessage(
                        userId, reportId, inputMessage);

        if (duplicate == null) {
            return rejected(reportId, "REPORT_MESSAGE_CONFLICT", errorCode.getMessage());
        }

        return SocketEventResponse.of(
                EVENT_DUPLICATE,
                SocketReportMessageDuplicateResponse.of(
                        reportId,
                        duplicate.inputMessageId(),
                        duplicate.streamMessageId(),
                        "SUCCEEDED"));
    }

    private SocketEventResponse<SocketReportMessageSendRejectedResponse> rejected(
            Long reportId, String code, String message) {
        return SocketEventResponse.of(
                EVENT_REJECTED,
                SocketReportMessageSendRejectedResponse.of(reportId, code, message, false));
    }

    private SocketEventResponse<SocketReportMessageSendRejectedResponse> failed(
            Long reportId, String code, String message) {
        return SocketEventResponse.of(
                EVENT_FAILED,
                SocketReportMessageSendRejectedResponse.of(reportId, code, message, true));
    }

    private SocketEventResponse<SocketReportMessageCancelRejectedResponse>
            handleCancelBusinessError(Long reportId, Long messageId, ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_REQUEST_MISSING_REQUIRED, INVALID_REQUEST_REQUIRED_VALUES ->
                    cancelRejected(
                            reportId,
                            messageId,
                            "REPORT_MESSAGE_CANCEL_INVALID_PAYLOAD",
                            errorCode.getMessage());

            case FORBIDDEN_REPORT_ACCESS, FORBIDDEN_REPORT_NOT_AVAILABLE_YET ->
                    cancelRejected(
                            reportId,
                            messageId,
                            "REPORT_MESSAGE_CANCEL_FORBIDDEN",
                            errorCode.getMessage());

            case REPORT_NOT_FOUND_GENERIC, MESSAGE_NOT_FOUND ->
                    cancelRejected(
                            reportId,
                            messageId,
                            "REPORT_MESSAGE_CANCEL_NOT_FOUND",
                            errorCode.getMessage());

            case CONFLICT_RESPONSE_ALREADY_ENDED, CONFLICT_STREAM_ENDED ->
                    cancelRejected(
                            reportId,
                            messageId,
                            "REPORT_MESSAGE_CANCEL_INVALID_STATE",
                            errorCode.getMessage());

            default ->
                    cancelFailed(
                            reportId,
                            messageId,
                            "REPORT_MESSAGE_CANCEL_INTERNAL_ERROR",
                            errorCode.getMessage());
        };
    }

    private SocketEventResponse<SocketReportMessageCancelRejectedResponse> cancelRejected(
            Long reportId, Long messageId, String code, String message) {
        return SocketEventResponse.of(
                EVENT_CANCEL_REJECTED,
                SocketReportMessageCancelRejectedResponse.of(
                        reportId, messageId, code, message, false));
    }

    private SocketEventResponse<SocketReportMessageCancelRejectedResponse> cancelFailed(
            Long reportId, Long messageId, String code, String message) {
        return SocketEventResponse.of(
                EVENT_CANCEL_FAILED,
                SocketReportMessageCancelRejectedResponse.of(
                        reportId, messageId, code, message, true));
    }
}
