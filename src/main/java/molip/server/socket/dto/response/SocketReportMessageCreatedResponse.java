package molip.server.socket.dto.response;

import java.time.OffsetDateTime;
import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;

public record SocketReportMessageCreatedResponse(
        String eventId,
        Long reportId,
        Long messageId,
        SenderType senderType,
        MessageType messageType,
        String content,
        OffsetDateTime sentAt) {

    public static SocketReportMessageCreatedResponse of(
            String eventId,
            Long reportId,
            Long messageId,
            SenderType senderType,
            MessageType messageType,
            String content,
            OffsetDateTime sentAt) {
        return new SocketReportMessageCreatedResponse(
                eventId, reportId, messageId, senderType, messageType, content, sentAt);
    }
}
