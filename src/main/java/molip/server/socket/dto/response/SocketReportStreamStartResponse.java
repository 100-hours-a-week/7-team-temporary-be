package molip.server.socket.dto.response;

import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;

public record SocketReportStreamStartResponse(
        Long reportId,
        Long messageId,
        SenderType senderType,
        MessageType messageType,
        String status) {

    public static SocketReportStreamStartResponse of(
            Long reportId,
            Long messageId,
            SenderType senderType,
            MessageType messageType,
            String status) {
        return new SocketReportStreamStartResponse(
                reportId, messageId, senderType, messageType, status);
    }
}
