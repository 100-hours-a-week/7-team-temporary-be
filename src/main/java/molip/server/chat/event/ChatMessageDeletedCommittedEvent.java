package molip.server.chat.event;

import java.util.List;
import molip.server.socket.dto.response.SocketMessageDeletedResponse;
import molip.server.socket.dto.response.SocketUnreadChangedResponse;

public record ChatMessageDeletedCommittedEvent(
        Long roomId,
        SocketMessageDeletedResponse messageDeleted,
        List<SocketUnreadChangedResponse> unreadChanges) {}
