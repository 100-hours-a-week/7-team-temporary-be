package molip.server.ai.dto.request;

import molip.server.common.enums.MessageType;
import molip.server.common.enums.SenderType;

public record AiReportChatMessageRequest(
        Long messageId, SenderType senderType, MessageType messageType, String content) {

    public static AiReportChatMessageRequest of(
            Long messageId, SenderType senderType, MessageType messageType, String content) {

        return new AiReportChatMessageRequest(messageId, senderType, messageType, content);
    }
}
