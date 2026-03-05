package molip.server.chat.dto.response;

import java.time.OffsetDateTime;
import molip.server.common.response.ImageInfoResponse;

public record ChatMessageSendResponse(
        Long messageId,
        String idempotencyKey,
        String delivery,
        String senderNickname,
        ImageInfoResponse senderProfile,
        OffsetDateTime sentAt) {

    public static ChatMessageSendResponse of(
            Long messageId,
            String idempotencyKey,
            String delivery,
            String senderNickname,
            ImageInfoResponse senderProfile,
            OffsetDateTime sentAt) {
        return new ChatMessageSendResponse(
                messageId, idempotencyKey, delivery, senderNickname, senderProfile, sentAt);
    }
}
