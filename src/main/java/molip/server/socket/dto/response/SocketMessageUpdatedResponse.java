package molip.server.socket.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import molip.server.chat.dto.response.MessageImageInfoResponse;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;
import molip.server.common.response.ImageInfoResponse;

public record SocketMessageUpdatedResponse(
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
        OffsetDateTime editedAt) {

    public static SocketMessageUpdatedResponse of(
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
            OffsetDateTime editedAt) {

        return new SocketMessageUpdatedResponse(
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
                editedAt);
    }
}
