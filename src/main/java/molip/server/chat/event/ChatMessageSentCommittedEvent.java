package molip.server.chat.event;

import java.util.List;
import molip.server.chat.dto.response.ChatMessageCreatedResponse;
import molip.server.socket.dto.response.SocketUnreadChangedResponse;

public record ChatMessageSentCommittedEvent(
        Long roomId,
        ChatMessageCreatedResponse messageCreated,
        List<SocketUnreadChangedResponse> unreadChanges) {}
