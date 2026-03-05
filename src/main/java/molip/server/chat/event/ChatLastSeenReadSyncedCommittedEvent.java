package molip.server.chat.event;

import molip.server.socket.dto.response.SocketUnreadChangedResponse;

public record ChatLastSeenReadSyncedCommittedEvent(
        Long userId, SocketUnreadChangedResponse unreadChanged) {}
