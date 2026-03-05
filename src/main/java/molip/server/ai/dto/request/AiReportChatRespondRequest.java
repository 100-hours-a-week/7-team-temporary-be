package molip.server.ai.dto.request;

import java.util.List;

public record AiReportChatRespondRequest(
        Long userId, Long messageId, List<AiReportChatMessageRequest> messages) {

    public static AiReportChatRespondRequest of(
            Long userId, Long messageId, List<AiReportChatMessageRequest> messages) {

        return new AiReportChatRespondRequest(userId, messageId, messages);
    }
}
