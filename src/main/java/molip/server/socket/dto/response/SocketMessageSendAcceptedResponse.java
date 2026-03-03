package molip.server.socket.dto.response;

import java.time.OffsetDateTime;

public record SocketMessageSendAcceptedResponse(
        String idempotencyKey, Long messageId, String status, OffsetDateTime sentAt) {

    public static SocketMessageSendAcceptedResponse of(
            String idempotencyKey, Long messageId, String status, OffsetDateTime sentAt) {
        return new SocketMessageSendAcceptedResponse(idempotencyKey, messageId, status, sentAt);
    }
}
