package molip.server.chat.dto.request;

import java.util.List;
import molip.server.common.enums.MessageType;

public record ChatMessageSendRequest(
        String idempotencyKey, MessageType messageType, String content, List<String> imageKeys) {}
