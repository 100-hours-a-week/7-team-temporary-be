package molip.server.chat.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;

public record ChatMessageCreatedResponse(
        String eventId,
        Long messageId,
        Long roomId,
        MessageType messageType,
        SenderType senderType,
        Long senderId,
        String content,
        List<MessageImageInfoResponse> images,
        OffsetDateTime sentAt) {

    public static ChatMessageCreatedResponse of(
            String eventId,
            Long messageId,
            Long roomId,
            MessageType messageType,
            SenderType senderType,
            Long senderId,
            String content,
            List<MessageImageInfoResponse> images,
            OffsetDateTime sentAt) {
        return new ChatMessageCreatedResponse(
                eventId,
                messageId,
                roomId,
                messageType,
                senderType,
                senderId,
                content,
                images,
                sentAt);
    }
}
