package molip.server.socket.dto.response;

import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;

public record SocketReportStreamChunkResponse(
        Long reportId,
        Long messageId,
        SenderType senderType,
        MessageType messageType,
        String delta,
        Long sequence) {

    public static SocketReportStreamChunkResponse of(
            Long reportId,
            Long messageId,
            SenderType senderType,
            MessageType messageType,
            String delta,
            Long sequence) {
        return new SocketReportStreamChunkResponse(
                reportId, messageId, senderType, messageType, delta, sequence);
    }
}
