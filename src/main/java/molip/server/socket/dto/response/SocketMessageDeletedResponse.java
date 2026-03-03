package molip.server.socket.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import molip.server.chat.dto.response.MessageImageInfoResponse;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;
import molip.server.common.response.ImageInfoResponse;

public record SocketMessageDeletedResponse(
        String eventId,
        Long messageId,
        Long roomId,
        MessageType messageType,
        SenderType senderType,
        Long senderId,
        String senderNickname,
        ImageInfoResponse senderProfile,
        String content,
        List<MessageImageInfoResponse> images,
        OffsetDateTime deletedAt) {

    public static SocketMessageDeletedResponse of(
            String eventId,
            Long messageId,
            Long roomId,
            MessageType messageType,
            SenderType senderType,
            Long senderId,
            String senderNickname,
            ImageInfoResponse senderProfile,
            String content,
            List<MessageImageInfoResponse> images,
            OffsetDateTime deletedAt) {

        return new SocketMessageDeletedResponse(
                eventId,
                messageId,
                roomId,
                messageType,
                senderType,
                senderId,
                senderNickname,
                senderProfile,
                content,
                images,
                deletedAt);
    }
}
