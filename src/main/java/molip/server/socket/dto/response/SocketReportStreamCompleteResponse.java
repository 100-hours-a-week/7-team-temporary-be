package molip.server.socket.dto.response;

import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;

public record SocketReportStreamCompleteResponse(
        Long reportId,
        Long messageId,
        SenderType senderType,
        MessageType messageType,
        String status) {

    public static SocketReportStreamCompleteResponse of(
            Long reportId,
            Long messageId,
            SenderType senderType,
            MessageType messageType,
            String status) {
        return new SocketReportStreamCompleteResponse(
                reportId, messageId, senderType, messageType, status);
    }
}
