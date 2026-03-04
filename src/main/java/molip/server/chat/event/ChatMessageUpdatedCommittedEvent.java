package molip.server.chat.event;

import java.util.List;
import molip.server.socket.dto.response.SocketMessageUpdatedResponse;
import molip.server.socket.dto.response.SocketUnreadChangedResponse;

public record ChatMessageUpdatedCommittedEvent(
        Long roomId,
        SocketMessageUpdatedResponse messageUpdated,
        List<SocketUnreadChangedResponse> unreadChanges) {}
