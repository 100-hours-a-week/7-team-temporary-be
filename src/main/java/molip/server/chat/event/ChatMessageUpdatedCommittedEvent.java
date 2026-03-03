package molip.server.chat.event;

import molip.server.socket.dto.response.SocketMessageUpdatedResponse;

public record ChatMessageUpdatedCommittedEvent(
        Long roomId, SocketMessageUpdatedResponse messageUpdated) {}
