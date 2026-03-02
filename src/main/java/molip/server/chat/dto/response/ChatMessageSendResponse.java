package molip.server.chat.dto.response;

import java.time.OffsetDateTime;

public record ChatMessageSendResponse(
        Long messageId, String idempotencyKey, String delivery, OffsetDateTime sentAt) {

    public static ChatMessageSendResponse of(
            Long messageId, String idempotencyKey, String delivery, OffsetDateTime sentAt) {
        return new ChatMessageSendResponse(messageId, idempotencyKey, delivery, sentAt);
    }
}
