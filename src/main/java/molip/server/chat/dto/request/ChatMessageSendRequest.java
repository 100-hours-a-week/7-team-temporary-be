package molip.server.chat.dto.request;

import java.util.List;

public record ChatMessageSendRequest(
        String idempotencyKey, String messageType, String content, List<String> imageKeys) {}
