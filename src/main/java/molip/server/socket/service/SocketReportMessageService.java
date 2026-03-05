package molip.server.socket.service;

import lombok.RequiredArgsConstructor;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.report.dto.response.ReportMessageCreateResponse;
import molip.server.report.facade.ReportCommandFacade;
import molip.server.socket.dto.request.SocketReportMessageSendRequest;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.dto.response.SocketReportMessageSendAcceptedResponse;
import molip.server.socket.dto.response.SocketReportMessageSendRejectedResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocketReportMessageService {

    private static final String EVENT_ACCEPTED = "report.message.sendAccepted";
    private static final String EVENT_REJECTED = "report.message.sendRejected";
    private static final String EVENT_FAILED = "report.message.sendFailed";

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

            return handleBusinessError(request.reportId(), exception.getErrorCode());

        } catch (Exception exception) {

            return failed(
                    request.reportId(),
                    "REPORT_MESSAGE_INTERNAL_ERROR",
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    private SocketEventResponse<SocketReportMessageSendRejectedResponse> handleBusinessError(
            Long reportId, ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_REQUEST_INPUT_MESSAGE_REQUIRED ->
                    rejected(reportId, "REPORT_MESSAGE_INVALID_PAYLOAD", errorCode.getMessage());

            case FORBIDDEN_REPORT_ACCESS, FORBIDDEN_REPORT_NOT_AVAILABLE_YET ->
                    rejected(reportId, "REPORT_MESSAGE_FORBIDDEN", errorCode.getMessage());

            case REPORT_NOT_FOUND_GENERIC ->
                    rejected(reportId, "REPORT_MESSAGE_NOT_FOUND", errorCode.getMessage());

            case CONFLICT_REPORT_RESPONSE_RUNNING ->
                    rejected(reportId, "REPORT_MESSAGE_CONFLICT", errorCode.getMessage());

            default -> failed(reportId, "REPORT_MESSAGE_INTERNAL_ERROR", errorCode.getMessage());
        };
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
}
