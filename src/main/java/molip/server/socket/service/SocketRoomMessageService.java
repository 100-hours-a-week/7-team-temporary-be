package molip.server.socket.service;

import lombok.RequiredArgsConstructor;
import molip.server.chat.dto.request.ChatMessageSendRequest;
import molip.server.chat.dto.response.ChatMessageSendCommandResult;
import molip.server.chat.dto.response.ChatMessageSendResponse;
import molip.server.chat.facade.ChatRoomCommandFacade;
import molip.server.common.exception.BaseException;
import molip.server.common.exception.ErrorCode;
import molip.server.socket.dto.request.SocketMessageSendRequest;
import molip.server.socket.dto.response.SocketEventResponse;
import molip.server.socket.dto.response.SocketMessageDuplicateResponse;
import molip.server.socket.dto.response.SocketMessageSendAcceptedResponse;
import molip.server.socket.dto.response.SocketMessageSendRejectedResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocketRoomMessageService {

    private static final String EVENT_ACCEPTED = "message.sendAccepted";
    private static final String EVENT_REJECTED = "message.sendRejected";
    private static final String EVENT_FAILED = "message.sendFailed";
    private static final String EVENT_DUPLICATE = "message.duplicate";

    private final ChatRoomCommandFacade chatRoomCommandFacade;

    public SocketEventResponse<?> sendMessage(Long userId, SocketMessageSendRequest request) {
        if (userId == null || request == null || request.roomId() == null) {
            return rejected(
                    request == null ? null : request.idempotencyKey(),
                    "MESSAGE_SEND_INVALID_PAYLOAD",
                    ErrorCode.INVALID_REQUEST_MESSAGE_SEND.getMessage());
        }

        try {
            ChatMessageSendCommandResult result =
                    chatRoomCommandFacade.sendMessageFallback(
                            userId,
                            request.roomId(),
                            new ChatMessageSendRequest(
                                    request.idempotencyKey(),
                                    request.messageType(),
                                    request.content(),
                                    request.imageKeys()));

            return toSocketResponse(result);
        } catch (BaseException exception) {
            return handleBusinessError(request.idempotencyKey(), exception.getErrorCode());
        } catch (Exception exception) {
            return failed(
                    request.idempotencyKey(),
                    "MESSAGE_SEND_INTERNAL_ERROR",
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage());
        }
    }

    private SocketEventResponse<?> toSocketResponse(ChatMessageSendCommandResult result) {
        ChatMessageSendResponse response = result.body().data();

        if (result.httpStatus() == HttpStatus.OK) {
            return SocketEventResponse.of(
                    EVENT_ACCEPTED,
                    SocketMessageSendAcceptedResponse.of(
                            response.idempotencyKey(),
                            response.messageId(),
                            "SUCCEEDED",
                            response.sentAt()));
        }

        if (response != null && response.messageId() != null) {
            return SocketEventResponse.of(
                    EVENT_DUPLICATE,
                    SocketMessageDuplicateResponse.of(
                            response.idempotencyKey(), response.messageId(), "SUCCEEDED"));
        }

        return failed(
                response == null ? null : response.idempotencyKey(),
                "MESSAGE_SEND_RETRYABLE_ERROR",
                result.body().message());
    }

    private SocketEventResponse<SocketMessageSendRejectedResponse> handleBusinessError(
            String idempotencyKey, ErrorCode errorCode) {
        return switch (errorCode) {
            case INVALID_REQUEST_MESSAGE_SEND ->
                    rejected(
                            idempotencyKey, "MESSAGE_SEND_INVALID_PAYLOAD", errorCode.getMessage());
            case FORBIDDEN_MESSAGE_SEND ->
                    rejected(idempotencyKey, "MESSAGE_SEND_FORBIDDEN", errorCode.getMessage());
            case CHAT_ROOM_NOT_FOUND, ROOM_NOT_FOUND, NOT_FOUND_ROOM ->
                    rejected(idempotencyKey, "MESSAGE_SEND_ROOM_NOT_FOUND", errorCode.getMessage());
            default ->
                    failed(idempotencyKey, "MESSAGE_SEND_INTERNAL_ERROR", errorCode.getMessage());
        };
    }

    private SocketEventResponse<SocketMessageSendRejectedResponse> rejected(
            String idempotencyKey, String code, String message) {
        return SocketEventResponse.of(
                EVENT_REJECTED,
                SocketMessageSendRejectedResponse.of(idempotencyKey, code, message, false));
    }

    private SocketEventResponse<SocketMessageSendRejectedResponse> failed(
            String idempotencyKey, String code, String message) {
        return SocketEventResponse.of(
                EVENT_FAILED,
                SocketMessageSendRejectedResponse.of(idempotencyKey, code, message, true));
    }
}
