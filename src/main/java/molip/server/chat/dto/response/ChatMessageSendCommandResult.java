package molip.server.chat.dto.response;

import molip.server.common.SuccessCode;
import molip.server.common.exception.ErrorCode;
import molip.server.common.response.ServerResponse;
import org.springframework.http.HttpStatus;

public record ChatMessageSendCommandResult(
        HttpStatus httpStatus, ServerResponse<ChatMessageSendResponse> body) {

    public static ChatMessageSendCommandResult succeeded(ChatMessageSendResponse response) {
        return new ChatMessageSendCommandResult(
                HttpStatus.OK, ServerResponse.success(SuccessCode.CHAT_MESSAGE_CREATED, response));
    }

    public static ChatMessageSendCommandResult duplicated(ChatMessageSendResponse response) {
        return new ChatMessageSendCommandResult(
                HttpStatus.CONFLICT,
                new ServerResponse<>("CONFLICT", "동일한 요청이 이미 처리되었습니다.", response));
    }

    public static ChatMessageSendCommandResult processing(ChatMessageSendResponse response) {
        return new ChatMessageSendCommandResult(
                ErrorCode.CONFLICT_MESSAGE_REQUEST_PROCESSING.getStatus(),
                new ServerResponse<>(
                        ErrorCode.CONFLICT_MESSAGE_REQUEST_PROCESSING.getStatusValue(),
                        ErrorCode.CONFLICT_MESSAGE_REQUEST_PROCESSING.getMessage(),
                        response));
    }
}
