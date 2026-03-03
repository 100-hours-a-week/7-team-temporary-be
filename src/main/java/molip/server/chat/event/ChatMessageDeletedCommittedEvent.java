package molip.server.chat.event;

import molip.server.socket.dto.response.SocketMessageDeletedResponse;

public record ChatMessageDeletedCommittedEvent(
        Long roomId, SocketMessageDeletedResponse messageDeleted) {}
