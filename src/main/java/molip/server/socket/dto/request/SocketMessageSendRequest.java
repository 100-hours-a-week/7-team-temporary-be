package molip.server.socket.dto.request;

import java.util.List;
import molip.server.common.enums.MessageType;

public record SocketMessageSendRequest(
        Long roomId,
        String idempotencyKey,
        MessageType messageType,
        String content,
        List<String> imageKeys) {}
